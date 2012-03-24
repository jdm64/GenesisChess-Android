package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

class MsgListAdapter extends BaseAdapter implements ListAdapter
{
	private final Context context;
	private final String gameID;

	private GameDataDB db;
	private SQLiteCursor list;

	public MsgListAdapter(final Context _context, final String GameID)
	{
		super();

		context = _context;
		gameID = GameID;
		initCursor();

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String username = pref.getString("username", "");

		MsgListItem.Cache.Init(context, username);
	}

	private void initCursor()
	{
		db = new GameDataDB(context);
		list = db.getMsgList(gameID);
	}

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

	public long getItemId(final int index)
	{
		return index;
	}

	public Object getItem(final int index)
	{
		return GameDataDB.rowToBundle(list, index);
	}

	public View getView(final int index, View cell, final ViewGroup parent)
	{
		final Bundle data = (Bundle) getItem(index);

		cell = new MsgListItem(parent.getContext());
		((MsgListItem) cell).setData(data);

		return cell;
	}

	public View getEmptyView(final Context context)
	{
		final View cell = View.inflate(context, R.layout.msglist_cell_empty, null);

		// Fix sizing issue
		final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		cell.setLayoutParams(lp);

		return cell;
	}
}

class MsgListItem extends View
{
	private final TextPaint paint = new TextPaint();
	private Bundle data;
	private StaticLayout msgImg;
	private PrettyDate time;
	private int lastWidth;

	final static class Cache
	{
		public static Typeface fontNormal;
		public static Typeface fontItalic;
		public static String username;
		public static int dpi;

		public static int padding;
		public static int smallText;
		public static int largeText;
		public static int headerAlign;
		public static int headerHeight;

		private Cache()
		{
		}

		public static void Init(final Context context, final String Username)
		{
			fontNormal = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
			fontItalic = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
			username = Username;

			final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			dpi = (int) ((1 + Math.max(metrics.ydpi, metrics.xdpi)) / 160);

			padding = 9 * dpi;
			smallText = 20 * dpi;
			largeText = 22 * dpi;
			headerAlign = 22 * dpi;
			headerHeight = 30 * dpi;
		}
	}

	public MsgListItem(final Context context)
	{
		super(context);

		paint.setAntiAlias(true);
		paint.setTypeface(Cache.fontNormal);
		paint.setTextSize(Cache.largeText);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int width = MeasureSpec.getSize(widthMeasureSpec);

		if (width != lastWidth) {
			msgImg = new StaticLayout(data.getString("msg"), paint, width - 4 * Cache.padding, Alignment.ALIGN_NORMAL, 1, 0, true);
			lastWidth = width;
		}

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
		if (data.getString("username").equals(Cache.username))
			paint.setColor(0xffd2d0ff);
		else
			paint.setColor(0xffcce6ff);
		canvas.drawRect(0, 0, getWidth(), Cache.headerHeight, paint);

		// draw username
		paint.setColor(0xff000000);
		paint.setTextSize(Cache.smallText);
		canvas.drawText(data.getString("username"), Cache.padding, Cache.headerAlign, paint);

		// draw time
		paint.setTextAlign(Paint.Align.RIGHT);
		paint.setTypeface(Cache.fontItalic);
		canvas.drawText(time.agoFormat(), width - Cache.padding, Cache.headerAlign, paint);

		// reset paint
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTypeface(Cache.fontNormal);
		paint.setTextSize(Cache.largeText);
	}

	public void setData(final Bundle bundle)
	{
		data = bundle;
		time = new PrettyDate(data.getString("time"));
	}
}
