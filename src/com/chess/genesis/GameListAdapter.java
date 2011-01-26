package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import java.util.Date;

public class GameListAdapter extends BaseAdapter implements ListAdapter
{
	private SQLiteCursor list;

	public GameListAdapter(Context context)
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

		if (cell == null) {
			TableLayout newcell = new TableLayout(parent.getContext());
			newcell.inflate(parent.getContext(), R.layout.gamelist_cell, newcell);
			cell = newcell;
		}

		TextView txt = (TextView) cell.findViewById(R.id.game_name);
		txt.setText(data.getString("name"));

		String date = (new PrettyDate(data.getString("stime"))).stdFormat();

		txt = (TextView) cell.findViewById(R.id.game_date);
		txt.setText(date);

		return cell;
	}
}
