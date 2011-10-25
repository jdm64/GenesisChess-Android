package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

		if (cell == null) {
			final TableLayout newcell = new TableLayout(parent.getContext());
			newcell.inflate(parent.getContext(), R.layout.msglist_cell, newcell);
			cell = newcell;
		}
		TextView txt = (TextView) cell.findViewById(R.id.username);
		txt.setText(data.getString("username"));

		txt = (TextView) cell.findViewById(R.id.time);
		final String time = (new PrettyDate(data.getString("time"))).agoFormat();
		txt.setText(time);

		txt = (TextView) cell.findViewById(R.id.msg);
		txt.setText(data.getString("msg"));

		return cell;
	}

	public View getEmptyView(final Context context)
	{
		final RelativeLayout cell = new RelativeLayout(context);

		cell.inflate(context, R.layout.msglist_cell_empty, cell);

		return cell;
	}
}
