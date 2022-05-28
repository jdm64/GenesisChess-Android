/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.data;

import android.content.*;
import android.content.res.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;

public class GameListAdapter extends BaseAdapter
{
	private final Context context;
	private final Bundle settings;
	private final int type;

	private final BoardSquare whitePawn;
	private final BoardSquare blackPawn;
	private final PieceImgPainter piecePainter;

	private GameDataDB db;
	private SQLiteCursor list;
	private int yourturn;

	public GameListAdapter(final ListView listView, final int Type, final int yourTurn)
	{
		context = listView.getContext();

		final Pref pref = new Pref(context);
		type = Type;
		yourturn = yourTurn;

		piecePainter = new PieceImgPainter(context);
		whitePawn = new BoardSquare(listView, piecePainter, 0);
		whitePawn.setPiece(Piece.WHITE_PAWN);
		blackPawn = new BoardSquare(listView, piecePainter, 1);
		blackPawn.setPiece(Piece.BLACK_PAWN);

		settings = new Bundle();
		settings.putString(pref.key(R.array.pf_username), pref.getString(R.array.pf_username));
		settings.putInt("type", type);

		View empty = getEmptyView(context);
		((ViewGroup) listView.getParent()).addView(empty);
		listView.setEmptyView(empty);
		listView.setAdapter(this);
	}

	public Context getContext()
	{
		return context;
	}

	private SQLiteCursor getCursor()
	{
		if (list == null) {
			db = new GameDataDB(context);
			switch (type) {
			case Enums.LOCAL_GAME:
				list = db.getLocalGameList();
				break;
			case Enums.ONLINE_GAME:
				list = db.getOnlineGameList(yourturn);
				break;
			case Enums.ARCHIVE_GAME:
				list = db.getArchiveGameList();
				break;
			}
		}
		return list;
	}

	public PieceImgPainter getPiecePainter()
	{
		return piecePainter;
	}

	public BoardSquare getWhitePawn()
	{
		return whitePawn;
	}

	public BoardSquare getBlackPawn()
	{
		return blackPawn;
	}

	@Override
	public int getCount()
	{
		return getCursor().getCount();
	}

	public Bundle getExtras()
	{
		return settings;
	}

	public void update()
	{
		if (list.isClosed())
			getCursor();
		else
			list.requery();
		notifyDataSetChanged();
	}

	public void setYourturn(final int yourTurn)
	{
		yourturn = yourTurn;
		list = db.getOnlineGameList(yourturn);
		notifyDataSetChanged();
	}

	public void close()
	{
		list.close();
		db.close();
	}

	@Override
	public long getItemId(final int index)
	{
		return index;
	}

	@Override
	public Object getItem(final int index)
	{
		final Bundle data = GameDataDB.rowToBundle(list, index, false);
		data.putAll(settings);

		return data;
	}

	@Override
	public View getView(final int index, View cell, final ViewGroup parent)
	{
		final Bundle data = (Bundle) getItem(index);

		if (cell == null)
			cell = new GameListItem(this);
		((GameListItem) cell).setData(data, index);

		return cell;
	}

	private View getEmptyView(final Context _context)
	{
		final View cell = View.inflate(_context, R.layout.gamelist_cell_empty, null);
		final TextView txt = cell.findViewById(R.id.message);

		// Fix sizing issue
		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		cell.setLayoutParams(lp);

		switch (type) {
		case Enums.LOCAL_GAME:
		case Enums.ONLINE_GAME:
			txt.setText("Click the plus button to create a game");
			break;
		case Enums.ARCHIVE_GAME:
			txt.setText("Finished online games will appear here");
			break;
		}
		return cell;
	}

	public int getType()
	{
		return type;
	}
}

class GameListItem extends View
{
	private final GameListAdapter adapter;
	private final Paint paint = new Paint();
	private DataItem data;
	private int index;

	final static class DataItem
	{
		public String name;
		public String type;
		public String time;
		public int gametype;
		public int opponent;
		public int status;
		public int idle;
		public int ply;
		public boolean hasMsg;
	}

	public GameListItem(final GameListAdapter _adapter)
	{
		super(_adapter.getContext());

		adapter = _adapter;
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		int size = (int) ((5.0 * MeasureSpec.getSize(widthMeasureSpec)) / 32.0);
		adapter.getPiecePainter().resize(size);
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), size);
	}

	private static float luminance(int color)
	{
		return (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		TypedArray arr = adapter.getContext().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
		int txtColor = arr.getColor(0, MColors.BLACK);
		arr.recycle();

		paint.setColor(txtColor);

		// background color
		if (!isPressed() && index % 2 != 0)
			canvas.drawColor(luminance(txtColor) < 0.5 ? MColors.CYAN_50 : MColors.BLUE_NAVY_400);

		// icon
		setIcon(canvas);

		int size = adapter.getPiecePainter().getSize();
		// game name
		paint.setTextSize(size * 5 / 12);
		canvas.drawText(data.name, size * 9 / 8, size / 2, paint);

		// set italic text
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

		// game type
		paint.setTextSize(size / 4);
		canvas.drawText(data.type, size * 9 / 8, size * 7 / 8, paint);

		// time
		paint.setTextAlign(Paint.Align.RIGHT);
		canvas.drawText(data.time, getWidth() - (size / 8), size * 7 / 8, paint);

		if (data.gametype != Enums.LOCAL_GAME)
			setOnlineTxt(canvas);

		// reset paint
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
	}

	private void setIcon(final Canvas canvas)
	{
		final BoardSquare img;

		BoardSquare whitePawn = adapter.getWhitePawn();
		BoardSquare blackPawn = adapter.getBlackPawn();

		switch (data.gametype) {
		case Enums.LOCAL_GAME:
			switch (data.opponent) {
			case Enums.CPU_BLACK_OPPONENT:
				img = whitePawn;
				break;
			case Enums.CPU_WHITE_OPPONENT:
				img = blackPawn;
				break;
			case Enums.HUMAN_OPPONENT:
			default:
				img = (index % 2 == 0)?
					blackPawn :
					whitePawn;
				break;
			}
			break;
		case Enums.ONLINE_GAME:
		default:
			img = (data.ply % 2 == 0)?
				whitePawn :
				blackPawn;
			break;
		case Enums.ARCHIVE_GAME:
			switch (data.status) {
			case Enums.WHITEMATE:
			case Enums.BLACKRESIGN:
			case Enums.BLACKIDLE:
				img = whitePawn;
				break;
			case Enums.BLACKMATE:
			case Enums.WHITERESIGN:
			case Enums.WHITEIDLE:
				img = blackPawn;
				break;
			case Enums.STALEMATE:
			case Enums.IMPOSSIBLE:
			default:
				img = (index % 2 == 0)?
					blackPawn :
					whitePawn;
				break;
			}
			break;
		}
		img.draw(canvas);
	}

	private void setOnlineTxt(final Canvas canvas)
	{
		int border = getWidth() - 8;

		int size = adapter.getPiecePainter().getSize();
		if (data.hasMsg) {
			final String txt = "[new msg]";
			paint.setColor(MColors.GREEN_LIGHT_A700);
			canvas.drawText(txt, border, size / 2, paint);
			border -= paint.measureText(txt);
		}

		if (data.gametype == Enums.ARCHIVE_GAME)
			return;

		switch (data.idle) {
		case Enums.IDLE:
			paint.setColor(MColors.BLUE_800);
			canvas.drawText("[idle]", border, size / 2, paint);
			break;
		case Enums.NUDGED:
			paint.setColor(MColors.ORANGE_500);
			canvas.drawText("[nudged]", border, size / 2, paint);
			break;
		case Enums.CLOSE:
			paint.setColor(MColors.RED_A700);
			canvas.drawText("[close]", border, size / 2, paint);
			break;
		}
	}

	public void setData(final Bundle bundle, final int Index)
	{
		final String username = bundle.getString("username");

		index = Index;
		data = new DataItem();

		data.time = new PrettyDate(bundle.getString("stime")).agoFormat();
		data.gametype = bundle.getInt("type");
		data.name = bundle.getString(data.gametype == Enums.LOCAL_GAME ? "name" : username.equals(bundle.getString("white")) ? "black" : "white");
		data.type = (data.gametype == Enums.LOCAL_GAME ? Enums.OpponentType(Integer.parseInt(bundle.getString("opponent"))) : Enums.EventType(Integer.parseInt(bundle.getString("eventtype")))) + ' ' +
		Enums.GameType(Integer.parseInt(bundle.getString("gametype")));

		if (data.gametype != Enums.LOCAL_GAME) {
			data.ply = Integer.parseInt(bundle.getString("ply"));
			data.status = Integer.parseInt(bundle.getString("status"));
			data.hasMsg = (bundle.getString("unread") != null
				&& bundle.getString("unread").equals("1"));

			if (data.gametype == Enums.ONLINE_GAME)
				data.idle = Integer.parseInt(bundle.getString("idle"));
		} else {
			data.opponent = Integer.parseInt(bundle.getString("opponent"));
		}
	}
}
