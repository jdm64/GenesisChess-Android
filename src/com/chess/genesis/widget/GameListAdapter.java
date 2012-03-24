/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

class GameListAdapter extends BaseAdapter implements ListAdapter
{
	private final Context context;
	private final Bundle settings;
	private final String username;
	private final int type;

	private GameDataDB db;
	private SQLiteCursor list;
	private int yourturn;

	public GameListAdapter(final Context _context, final int Type, final int yourTurn)
	{
		super();
		context = _context;

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		username = prefs.getString("username", "!error!");
		type = Type;
		yourturn = yourTurn;

		settings = new Bundle();
		settings.putString("username", username);
		settings.putInt("type", type);

		initCursor();
		GameListItem.Cache.Init(context);
	}

	private void initCursor()
	{
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

	public int getCount()
	{
		return list.getCount();
	}

	public Bundle getExtras()
	{
		return settings;
	}

	public void update()
	{
		if (list.isClosed())
			initCursor();
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

	public long getItemId(final int index)
	{
		return index;
	}

	public Object getItem(final int index)
	{
		final Bundle data = GameDataDB.rowToBundle(list, index);
		data.putAll(settings);

		return data;
	}

	public View getView(final int index, View cell, final ViewGroup parent)
	{
		final Bundle data = (Bundle) getItem(index);

		if (cell == null)
			cell = new GameListItem(parent.getContext());

		((GameListItem) cell).setData(data, index);
		return cell;
	}

	public View getEmptyView(final Context context)
	{
		final View cell = View.inflate(context, R.layout.gamelist_cell_empty, null);
		final TextView txt = (TextView) cell.findViewById(R.id.message);

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
}

class GameListItem extends View
{
	private final Paint paint = new Paint();
	private Bundle data;
	private String username;
	private PrettyDate time;
	private int gametype;
	private int index;

	final static class Cache
	{
		public static Bitmap whitePawn;
		public static Bitmap blackPawn;
		public static Typeface fontNormal;
		public static Typeface fontItalic;
		public static RectF rect;
		public static int dpi;
		public static int height;

		private Cache()
		{
		}

		public static void Init(final Context context)
		{
			fontNormal = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
			fontItalic = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
			rect = new RectF(0, 0, Cache.height, Cache.height);

			final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			dpi = (int) ((1 + Math.max(metrics.ydpi, metrics.xdpi)) / 160);
			height = 75 * dpi;

			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_pawn_light);
			blackPawn = Bitmap.createScaledBitmap(bm, 75 * dpi, 75 * dpi, true);
			bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_pawn_dark);
			whitePawn = Bitmap.createScaledBitmap(bm, 75 * dpi, 75 * dpi, true);
		}
	}

	public GameListItem(final Context context)
	{
		super(context);

		paint.setAntiAlias(true);
		paint.setTypeface(Cache.fontNormal);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Cache.height);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		// background color
		if (!isPressed() && index % 2 == 1)
			canvas.drawColor(0xffe2f4fb);

		// icon
		setIcon(canvas);

		// game name
		final String gamename = (gametype == Enums.LOCAL_GAME)?
			data.getString("name") :
			(username.equals(data.getString("white"))?
				data.getString("black") :
				data.getString("white"));
		paint.setTextSize(30 * Cache.dpi);
		canvas.drawText(gamename, Cache.height + 8, Cache.height / 2, paint);

		// set italic text
		paint.setTypeface(Cache.fontItalic);

		// game type
		final String type = (gametype == Enums.LOCAL_GAME)?
			Enums.OpponentType(Integer.valueOf(data.getString("opponent"))) + " " +
			Enums.GameType(Integer.valueOf(data.getString("gametype")))
			:
			Enums.EventType(Integer.valueOf(data.getString("eventtype"))) + " " +
			Enums.GameType(Integer.valueOf(data.getString("gametype")));
		paint.setTextSize(20 * Cache.dpi);
		canvas.drawText(type, Cache.height + 8, 7 * Cache.height / 8, paint);

		// time
		paint.setTextAlign(Paint.Align.RIGHT);
		canvas.drawText(time.agoFormat(), getWidth() - 8, 7 * Cache.height / 8, paint);

		if (gametype != Enums.LOCAL_GAME)
			setOnlineTxt(canvas);

		// reset paint
		paint.setColor(0xff000000);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTypeface(Cache.fontNormal);
	}

	private void setIcon(final Canvas canvas)
	{
		final Bitmap img;

		switch (gametype) {
		case Enums.LOCAL_GAME:
			switch (Integer.valueOf(data.getString("opponent"))) {
			case Enums.CPU_BLACK_OPPONENT:
				img = Cache.whitePawn;
				break;
			case Enums.CPU_WHITE_OPPONENT:
				img = Cache.blackPawn;
				break;
			case Enums.HUMAN_OPPONENT:
			default:
				img = (index % 2 == 0)?
					Cache.blackPawn :
					Cache.whitePawn;
				break;
			}
			break;
		case Enums.ONLINE_GAME:
		default:
			img = (Integer.valueOf(data.getString("ply")) % 2 == 0)?
				Cache.whitePawn :
				Cache.blackPawn;
			break;
		case Enums.ARCHIVE_GAME:
			switch (Integer.valueOf(data.getString("status"))) {
			case Enums.WHITEMATE:
			case Enums.BLACKRESIGN:
			case Enums.BLACKIDLE:
				img = Cache.whitePawn;
				break;
			case Enums.BLACKMATE:
			case Enums.WHITERESIGN:
			case Enums.WHITEIDLE:
				img = Cache.blackPawn;
				break;
			case Enums.STALEMATE:
			case Enums.IMPOSSIBLE:
			default:
				img = (index % 2 == 0)?
					Cache.blackPawn :
					Cache.whitePawn;
				break;
			}
			break;
		}
		canvas.drawBitmap(img, null, Cache.rect, paint);
	}

	private void setOnlineTxt(final Canvas canvas)
	{
		int border = getWidth() - 8;

		if (data.getString("unread") != null && data.getString("unread").equals("1")) {
			final String txt = "[new msg]";
			paint.setColor(0xff4e9a06);
			canvas.drawText(txt, border, Cache.height / 2, paint);
			border -= paint.measureText(txt);
		}

		if (gametype == Enums.ARCHIVE_GAME)
			return;

		switch (Integer.valueOf(data.getString("idle"))) {
		case Enums.IDLE:
			paint.setColor(0xff3465a4);
			canvas.drawText("[idle]", border, Cache.height / 2, paint);
			break;
		case Enums.NUDGED:
			paint.setColor(0xfff57900);
			canvas.drawText("[nudged]", border, Cache.height / 2, paint);
			break;
		case Enums.CLOSE:
			paint.setColor(0xffcc0000);
			canvas.drawText("[close]", border, Cache.height / 2, paint);
			break;
		}
	}

	public void setData(final Bundle bundle, final int Index)
	{
		data = bundle;
		index = Index;
		gametype = data.getInt("type");
		username = data.getString("username");
		time = new PrettyDate(data.getString("stime"));
	}
}
