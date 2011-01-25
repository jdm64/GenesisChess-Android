package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Date;

public class GameListLayout extends LinearLayout implements OnItemClickListener, OnTouchListener
{
	public static final int TOPBAR = 1;
	public static final int NEW_GAME = 2;

	public GameList gamelist;
	public ListView gamelistview;

	public GameListLayout(Context context)
	{
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		LinearLayout table = new LinearLayout(context);

		MyImageView button = new MyImageView(context);
		button.setImageResource(R.drawable.topbar_genesis);
		button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		button.setId(TOPBAR);
		button.setOnTouchListener(this);
		button.setOnClickListener(GameListActivity.self);
		table.addView(button);

		button = new MyImageView(context);
		button.setImageResource(R.drawable.topbar_plus);
		button.setId(NEW_GAME);
		button.setOnClickListener(GameListActivity.self);
		button.setOnTouchListener(this);
		button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 6.35f));
		table.addView(button);

		addView(table);

		gamelist = new GameList(context);

		gamelistview = new ListView(context);
		gamelistview.setAdapter(gamelist);
		gamelistview.setOnItemClickListener(this);
		addView(gamelistview);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		GameListActivity.self.selectGame(parent, position);
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		switch (v.getId()) {
		case NEW_GAME:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.topbar_plus_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.topbar_plus);
			break;
		case TOPBAR:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.topbar_genesis_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.topbar_genesis);
			break;
		}
		return false;
	}

	public void updateList()
	{
		gamelist.update();
	}

	public class GameList extends BaseAdapter implements ListAdapter
	{
		private SQLiteCursor list;

		public GameList(Context context)
		{
			GameDataDB db = new GameDataDB(context);
			list = db.getGameList();
		}

		public int getCount()
		{
			return list.getCount();
		}

		public void update()
		{
			list.requery();
			notifyDataSetChanged();
		}

		public long getItemId(int index)
		{
			return index;
		}

		public Object getItem(int index)
		{
			return GameDataDB.rowToBundle(list, index);
		}

		public View getView(int index, View cell, ViewGroup parent)
		{
			Bundle data = (Bundle) getItem(index);

			TableLayout newcell = new TableLayout(parent.getContext());
			newcell.setShrinkAllColumns(true);
			newcell.setStretchAllColumns(true);

			TableRow row = new TableRow(parent.getContext());
			
			TextView txt = new TextView(parent.getContext());
			txt.setText("Name: ");
			row.addView(txt);
			
			txt = new TextView(parent.getContext());
			txt.setText(data.getString("name"));
			row.addView(txt);

			newcell.addView(row);

			row = new TableRow(parent.getContext());

			txt = new TextView(parent.getContext());
			txt.setText("Date: ");
			row.addView(txt);

			txt = new TextView(parent.getContext());

			long date = (new Long(data.getString("stime"))).longValue();
			String sdate = (new Date(date)).toString();

			txt.setText(sdate);
			row.addView(txt);

			newcell.addView(row);

			return newcell;
		}
	}
}
