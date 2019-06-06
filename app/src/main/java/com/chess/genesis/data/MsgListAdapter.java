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
import android.content.ClipboardManager;
import android.database.sqlite.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.Layout.Alignment;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.util.*;

public class MsgListAdapter extends BaseAdapter implements OnTouchListener, OnLongClickListener
{
	private final Context context;
	private final String gameID;

	private GameDataDB db;
	private SQLiteCursor list;

	public MsgListAdapter(final Context _context, final String GameID)
	{

		context = _context;
		gameID = GameID;
		initCursor();
	}

	private void initCursor()
	{
		db = new GameDataDB(context);
		list = db.getMsgList(gameID);
	}

	@Override
	public int getCount()
	{
		return list.getCount();
	}

	public void update()
	{
		if (list.isClosed())
			initCursor();
		else
			list.requery();
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
		return GameDataDB.rowToBundle(list, index, false);
	}

	@Override
	public View getView(final int index, View cell, final ViewGroup parent)
	{
		final Bundle data = (Bundle) getItem(index);

		// BUG: must always create new instance
		// or the message list gets corrupt
		cell = new MsgListItem(parent.getContext());
		((MsgListItem) cell).setData(data);
		cell.setOnLongClickListener(this);
		cell.setOnTouchListener(this);

		return cell;
	}

	public static View getEmptyView(final Context _context)
	{
		final View cell = View.inflate(_context, R.layout.msglist_cell_empty, null);

		// Fix sizing issue
		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		cell.setLayoutParams(lp);

		return cell;
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event)
	{
		v.setBackgroundColor((event.getAction() == MotionEvent.ACTION_DOWN)? MColors.BLUE_LIGHT_500 : MColors.CLEAR);
		return false;
	}

	@Override
	public boolean onLongClick(final View view)
	{
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		String str = ((MsgListItem) view).getData().msg;
		ClipData data = ClipData.newPlainText("chat msg", str);
		clipboard.setPrimaryClip(data);
		Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show();
		return true;
	}
}

class MsgListItem extends View
{
	private final TextPaint paint = new TextPaint();
	private StaticLayout msgImg;
	private DataItem data;

	final static class DataItem
	{
		public String username;
		public String time;
		public String msg;
		public boolean isYourMsg;
	}

	final static class Cache
	{
		public static String username;

		public static int padding;
		public static int smallText;
		public static int largeText;
		public static int headerAlign;
		public static int headerHeight;

		private static boolean isActive = false;

		private Cache()
		{
		}

		public static void Init(final Context context, final int width)
		{
			if (isActive)
				return;

			username = Pref.getString(context, R.array.pf_username);

			largeText = (int) ((3.0 / 64.0) * width);
			smallText = largeText - 2;
			padding = smallText / 2;
			headerAlign = largeText;
			headerHeight = (int) (1.5 * smallText);

			isActive = true;
		}
	}

	public MsgListItem(final Context context)
	{
		super(context);

		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
		paint.setTextSize(Cache.largeText);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		Cache.Init(getContext(), width);

		if (msgImg == null)
			msgImg = new StaticLayout(data.msg, paint, width - 4 * Cache.padding, Alignment.ALIGN_NORMAL, 1, 0, true);

		setMeasuredDimension(width, msgImg.getHeight() + Cache.headerHeight + 2 * Cache.padding);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		final int width = getMeasuredWidth();

		// draw msg text
		canvas.save();
		canvas.translate(2 * Cache.padding, Cache.headerHeight);
		msgImg.draw(canvas);
		canvas.restore();

		// draw msg header
		if (data.isYourMsg)
			paint.setColor(MColors.PURPLE_DEEP_100);
		else
			paint.setColor(MColors.TEAL_A100);
		canvas.drawRect(0, 0, getWidth(), Cache.headerHeight, paint);

		// draw username
		paint.setColor(MColors.BLACK);
		paint.setTextSize(Cache.smallText);
		canvas.drawText(data.username, Cache.padding, Cache.headerAlign, paint);

		// draw time
		paint.setTextAlign(Paint.Align.RIGHT);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
		canvas.drawText(data.time, width - Cache.padding, Cache.headerAlign, paint);

		// reset paint
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
		paint.setTextSize(Cache.largeText);
	}

	public void setData(final Bundle bundle)
	{
		data = new DataItem();

		data.username = bundle.getString("username");
		data.time = new PrettyDate(bundle.getString("time")).agoFormat();
		data.msg = bundle.getString("msg");
		data.isYourMsg = data.username.equals(Cache.username);
	}

	public DataItem getData()
	{
		return data;
	}
}
