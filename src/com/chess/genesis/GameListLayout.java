package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Date;

public class GameListLayout extends LinearLayout implements OnItemClickListener, OnItemLongClickListener
{
	public GameList gamelist;
	public ListView gamelistview;

	public GameListLayout(Context context)
	{
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		LinearLayout row = new LinearLayout(context);

		Button button = new Button(context);
		button.setText("New Game");
		button.setId(MainMenuActivity.LOCAL_GAME);
		button.setOnClickListener(GameListActivity.self);
		row.addView(button);

		addView(row);

		gamelist = new GameList(context);

		gamelistview = new ListView(context);
		gamelistview.setAdapter(gamelist);
		gamelistview.setOnItemClickListener(this);
		gamelistview.setOnItemLongClickListener(this);

		addView(gamelistview);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		GameListActivity.self.selectGame(parent, position);
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		return true;
	}

	public void updateList()
	{
		gamelist.update();
	}

	private class GameList extends BaseAdapter implements ListAdapter
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
