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

class GameListAdapter extends BaseAdapter implements ListAdapter
{
	private final GameDataDB db;
	private final String username;
	private final int type;

	private SQLiteCursor list;

	public GameListAdapter(final Context context, final Bundle settings)
	{
		super();

		db = new GameDataDB(context);
		username = settings.getString("username");
		type = settings.getInt("type");

		switch (type) {
		case Enums.LOCAL_GAME:
			list = db.getLocalGameList();
			break;
		case Enums.ONLINE_GAME:
			list = db.getOnlineGameList(1);
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

	public void update()
	{
		if (list.isClosed())
			return;
		list.requery();
		notifyDataSetChanged();
	}

	public void setYourturn(final int yourturn)
	{
		list = db.getOnlineGameList(yourturn);
		notifyDataSetChanged();
	}

	public void close()
	{
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
			switch (type) {
			case Enums.LOCAL_GAME:
				newcell.inflate(parent.getContext(), R.layout.gamelist_cell_local, newcell);
				break;
			case Enums.ONLINE_GAME:
				newcell.inflate(parent.getContext(), R.layout.gamelist_cell_online, newcell);
				break;
			case Enums.ARCHIVE_GAME:
				newcell.inflate(parent.getContext(), R.layout.gamelist_cell_archive, newcell);
				break;
			}
			cell = newcell;
		}
		switch (type) {
		case Enums.LOCAL_GAME:
			setupLocal(cell, data);
			break;
		case Enums.ONLINE_GAME:
			setupOnline(cell, data);
			break;
		case Enums.ARCHIVE_GAME:
			setupArchive(cell, data);
			break;
		}
		return cell;
	}

	private void setupLocal(final View cell, final Bundle data)
	{
		TextView txt = (TextView) cell.findViewById(R.id.game_name);
		txt.setText(data.getString("name"));

		final String date = (new PrettyDate(data.getString("stime"))).agoFormat();

		txt = (TextView) cell.findViewById(R.id.game_date);
		txt.setText(date);
	}

	private void setupOnline(final View cell, final Bundle data)
	{
		final String opponent = (username.equals(data.getString("white")))? data.getString("black") : data.getString("white");

		TextView txt = (TextView) cell.findViewById(R.id.game_opp);
		txt.setText(opponent);

		final String date = (new PrettyDate(data.getString("stime"))).agoFormat();

		txt = (TextView) cell.findViewById(R.id.game_date);
		txt.setText(date);
	}

	private void setupArchive(final View cell, final Bundle data)
	{
		final String opponent = (username.equals(data.getString("white")))? data.getString("black") : data.getString("white");

		TextView txt = (TextView) cell.findViewById(R.id.game_opp);
		txt.setText(opponent);

		final String date = (new PrettyDate(data.getString("stime"))).agoFormat();

		txt = (TextView) cell.findViewById(R.id.game_date);
		txt.setText(date);
	}
}
