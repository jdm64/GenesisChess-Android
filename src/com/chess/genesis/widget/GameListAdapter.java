package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.RelativeLayout;
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
		final ImageView img = (ImageView) cell.findViewById(R.id.cell_icon);
		img.setImageResource(R.drawable.white_pawn_dark);

		TextView txt = (TextView) cell.findViewById(R.id.game_name);
		txt.setText(data.getString("name"));

		final String type = Enums.OpponentType(Integer.valueOf(data.getString("opponent"))) + " " +
			Enums.GameType(Integer.valueOf(data.getString("gametype")));
		txt = (TextView) cell.findViewById(R.id.game_type);
		txt.setText(type);

		final String date = (new PrettyDate(data.getString("stime"))).agoFormat();
		txt = (TextView) cell.findViewById(R.id.game_time);
		txt.setText(date);
	}

	private void setupOnline(final View cell, final Bundle data)
	{
		final int icon = (Integer.valueOf(data.getString("ply")) % 2 == 0)?
			R.drawable.white_pawn_dark : R.drawable.black_pawn_light;
		final ImageView img = (ImageView) cell.findViewById(R.id.cell_icon);
		img.setImageResource(icon);

		final String opponent = (username.equals(data.getString("white")))? data.getString("black") : data.getString("white");
		TextView txt = (TextView) cell.findViewById(R.id.game_with);
		txt.setText(opponent);

		final String type = Enums.EventType(Integer.valueOf(data.getString("eventtype"))) + " " +
			Enums.GameType(Integer.valueOf(data.getString("gametype")));
		txt = (TextView) cell.findViewById(R.id.game_type);
		txt.setText(type);

		final String date = (new PrettyDate(data.getString("stime"))).agoFormat();
		txt = (TextView) cell.findViewById(R.id.game_time);
		txt.setText(date);

		txt = (TextView) cell.findViewById(R.id.idle);
		switch (Integer.valueOf(data.getString("idle"))) {
		case Enums.NOTIDLE:
			txt.setText("");
			break;
		case Enums.IDLE:
			txt.setText("[idle]");
			txt.setTextColor(0xff3465a4);
			break;
		case Enums.NUDGED:
			txt.setText("[nudged]");
			txt.setTextColor(0xfff57900);
			break;
		case Enums.CLOSE:
			txt.setText("[close]");
			txt.setTextColor(0xffcc0000);
			break;
		}

		txt = (TextView) cell.findViewById(R.id.new_msgs);
		if ((data.getString("unread") == null)? false : data.getString("unread").equals("1"))
			txt.setText("[new msg]");
		else
			txt.setText("");
	}

	private void setupArchive(final View cell, final Bundle data)
	{
		final int icon;
		switch (Integer.valueOf(data.getString("status"))) {
		case Enums.WHITEMATE:
		case Enums.BLACKRESIGN:
		case Enums.BLACKIDLE:
			icon = R.drawable.white_pawn_dark;
			break;
		case Enums.BLACKMATE:
		case Enums.WHITERESIGN:
		case Enums.WHITEIDLE:
			icon = R.drawable.black_pawn_light;
			break;
		case Enums.STALEMATE:
		case Enums.IMPOSSIBLE:
		default:
			icon = R.drawable.square_dark;
			break;
		}
		final ImageView img = (ImageView) cell.findViewById(R.id.cell_icon);
		img.setImageResource(icon);

		final String opponent = (username.equals(data.getString("white")))? data.getString("black") : data.getString("white");
		TextView txt = (TextView) cell.findViewById(R.id.game_with);
		txt.setText(opponent);

		final String type = Enums.EventType(Integer.valueOf(data.getString("eventtype"))) + " " +
			Enums.GameType(Integer.valueOf(data.getString("gametype")));
		txt = (TextView) cell.findViewById(R.id.game_type);
		txt.setText(type);

		final String date = (new PrettyDate(data.getString("stime"))).agoFormat();
		txt = (TextView) cell.findViewById(R.id.game_time);
		txt.setText(date);

		txt = (TextView) cell.findViewById(R.id.new_msgs);
		if ((data.getString("unread") == null)? false : data.getString("unread").equals("1"))
			txt.setText("[new msg]");
		else
			txt.setText("");
	}

	public View getEmptyView(final Context context)
	{
		final RelativeLayout cell = new RelativeLayout(context);

		cell.inflate(context, R.layout.gamelist_cell_empty, cell);

		final TextView txt = (TextView) cell.findViewById(R.id.message);

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
