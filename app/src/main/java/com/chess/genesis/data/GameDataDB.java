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

import java.io.*;
import java.util.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;

public class GameDataDB implements Closeable
{
	private final SQLiteDatabase db;
	private final Context context;

	public GameDataDB(final Context _context)
	{
		context = _context;
		db = new DatabaseOpenHelper(context).getWritableDatabase();
	}

	@Override
	public void close()
	{
		db.close();
	}

	public static Bundle rowToBundle(final Cursor cursor, final int index, final boolean closeCursor)
	{
		final Bundle bundle = new Bundle();
		final String[] column = cursor.getColumnNames();

		cursor.moveToPosition(index);
		for (int i = 0, len = cursor.getColumnCount(); i < len; i++)
			bundle.putString(column[i], cursor.getString(i));
		if (closeCursor)
			cursor.close();
		return bundle;
	}

	private String getUsername()
	{
		return Pref.getString(context, R.array.pf_username);
	}

	/*
	 * Local Game Queries
	 */

	public Bundle newLocalGame(final String gamename, final int gametype, final int opponent)
	{
		final long time = System.currentTimeMillis();
		final Object[] data = {gamename, time, time, gametype, opponent};
		final String[] data2 = {String.valueOf(time)};

		db.execSQL("INSERT INTO localgames (name, ctime, stime, gametype, opponent) VALUES (?, ?, ?, ?, ?);", data);
		final Bundle game = rowToBundle(db.rawQuery("SELECT * FROM localgames WHERE ctime=?", data2), 0, true);
		game.putInt("type", Enums.LOCAL_GAME);

		return game;
	}

	public void addLocalGame(final Bundle game)
	{
		final Object[] data = {game.getString("name"), game.getLong("ctime"),
			game.getLong("stime"), game.getInt("gametype"), game.getInt("opponent"),
			game.getString("history"), game.getString("zfen")};

		db.execSQL("INSERT INTO localgames (name, ctime, stime, gametype, opponent, history, zfen) VALUES (?, ?, ?, ?, ?, ?, ?);", data);
	}

	public void saveLocalGame(final int id, final long stime, final String zfen, final String history)
	{
		final Object[] data = {stime, zfen, history, id};
		db.execSQL("UPDATE localgames SET stime=?, zfen=?, history=? WHERE id=?;", data);
	}

	public void renameLocalGame(final int id, final String name)
	{
		final Object[] data = {name, id};
		db.execSQL("UPDATE localgames SET name=? WHERE id=?;", data);
	}

	public void deleteLocalGame(final int id)
	{
		final Object[] data = {id};
		db.execSQL("DELETE FROM localgames WHERE id=?;", data);
	}

	public void deleteAllLocalGames()
	{
		db.execSQL("DELETE FROM localgames;");
	}

	public SQLiteCursor getLocalGameList()
	{
		return (SQLiteCursor) db.rawQuery("SELECT * FROM localgames ORDER BY stime DESC", null);
	}

	public void copyGameToLocal(final String gameid, final int gametype)
	{
		final String[] data = {gameid};
		final String type = (gametype == Enums.ONLINE_GAME)? "onlinegames" : "archivegames";
		final Bundle row = rowToBundle(db.rawQuery("SELECT * FROM " + type + " WHERE gameid=?", data), 0, true);

		final long time = System.currentTimeMillis();
		final String tnames = "(name, ctime, stime, gametype, opponent, zfen, history)";
		final String dstring = "(?, ?, ?, ?, ?, ?, ?)";
		final Object[] data2 = {row.get("white") + " Vs. " + row.get("black"), time, time,
			row.get("gametype"), Enums.HUMAN_OPPONENT, row.get("zfen"), row.get("history")};

		db.execSQL("INSERT INTO localgames" + tnames + " VALUES " + dstring + ';', data2);
	}

	/*
	 * Online Game Queries
	 */

	public void insertOnlineGame(final JSONObject json)
	{
	try {
		final String gameid = json.getString("gameid");

		if (json.optBoolean("delete", false)) {
			deleteOnlineGame(gameid);
			return;
		}

		final String white = json.getString("white");
		final String black = json.getString("black");
		final String zfen = json.getString("zfen");
		final String history = json.getString("history");
		final long ctime = json.getLong("ctime");
		final long stime = json.getLong("stime");
		final int gametype = Enums.GameType(json.getString("gametype"));
		final int eventtype = Enums.EventType(json.getString("eventtype"));
		final int status = Enums.GameStatus(json.getString("status"));
		final int idle = (json.has("idle")? 1:0) + (json.has("nudge")? 1:0) + (json.has("close")? 1:0);
		final int drawoffer = json.has("drawoffer")? (json.getString("drawoffer").equals("white")? Piece.WHITE : Piece.BLACK) : 0;

		final GameInfo info = new GameInfo(context, status, history, white, drawoffer);

		final int ply = info.getPly(), yourturn = info.getYourTurn();

		final Object[] data = {gameid, gametype, eventtype, status, ctime,
			stime, yourturn, ply, white, black, zfen, history, idle, drawoffer};

		final String q1 = "INSERT OR REPLACE INTO onlinegames ";
		final String q2 = "(gameid, gametype, eventtype, status, ctime, stime, ";
		final String q3 = "yourturn, ply, white, black, zfen, history, idle, drawoffer) ";
		final String q4 = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		db.execSQL(q1 + q2 + q3 + q4, data);
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	public void updateOnlineGame(final JSONObject json)
	{
	try {
		final String gameid = json.getString("gameid");

		if (json.optBoolean("delete", false)) {
			deleteOnlineGame(gameid);
			return;
		}

		final String[] data1 = {gameid};
		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data1);

		if (cursor.getCount() < 1) {
			if (!checkArchiveGame(gameid))
				insertOnlineGame(json);
			cursor.close();
			return;
		}

		final String zfen = json.getString("zfen");
		final String history = json.getString("history");
		final long stime = json.getLong("stime");
		final int status = Enums.GameStatus(json.getString("status"));
		final int idle = (json.has("idle")? 1:0) + (json.has("nudge")? 1:0) + (json.has("close")? 1:0);
		final int drawoffer = json.has("drawoffer")? (json.getString("drawoffer").equals("white")? Piece.WHITE : Piece.BLACK) : 0;

		final Bundle row = rowToBundle(cursor, 0, true);
		final GameInfo info = new GameInfo(context, status, history, row.getString("white"), drawoffer);

		final int ply = info.getPly(), yourturn = info.getYourTurn();

		final Object[] data2 = {stime, status, ply, yourturn, zfen, history, idle, drawoffer, gameid};
		db.execSQL("UPDATE onlinegames SET stime=?, status=?, ply=?, yourturn=?, zfen=?, history=?, idle=?, drawoffer=? WHERE gameid=?;", data2);
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private void deleteOnlineGame(final String gameid)
	{
		final Object[] data = {gameid};
		db.execSQL("DELETE FROM archivegames WHERE gameid=?;", data);
	}

	public long getNewestOnlineTime()
	{
		final String username = getUsername();
		final String[] data = {username, username};

		final String query = "SELECT stime FROM onlinegames WHERE white=? OR black=? ORDER BY stime DESC LIMIT 1";

		try (SQLiteCursor cursor = (SQLiteCursor) db.rawQuery(query, data)) {
			if (cursor.getCount() == 0)
				return 0;
			cursor.moveToFirst();
			return cursor.getLong(0);
		}
	}

	public List<String> getOnlineGameIds()
	{
		final String username = getUsername();
		final String[] data = {username, username};
		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM onlinegames WHERE white=? OR black=?", data);
		final List<String> list = new ArrayList<>(cursor.getCount());

		cursor.moveToFirst();
		for (int i = 0, len = cursor.getCount(); i < len; i++) {
			list.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	public SQLiteCursor getOnlineGameList(final int yourturn)
	{
		final String username = getUsername();
		final String[] data = {username, username, String.valueOf(yourturn)};
		final String query =
			"SELECT * FROM onlinegames LEFT JOIN (SELECT gameid, unread FROM msgtable WHERE unread=1) USING(gameid) " +
			"WHERE (white=? OR black=?) AND yourturn=? GROUP BY gameid ORDER BY stime DESC";

		return (SQLiteCursor) db.rawQuery(query, data);
	}

	public Bundle getOnlineGameData(final String gameid)
	{
		return rowToBundle(db.rawQuery("SELECT * from onlinegames WHERE gameid=?", new String[]{gameid}), 0, true);
	}

	public void recalcYourTurn()
	{
		final String username = getUsername();
		final String[] data = {username, username};
		final String query = "SELECT gameid, status, history, white, drawoffer FROM onlinegames WHERE white=? or black=?;";

		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery(query, data);

		cursor.moveToFirst();
		for (int i = 0, len = cursor.getCount(); i < len; i++) {
			final String gameid = cursor.getString(0);
			final int status = cursor.getInt(1);
			final String history = cursor.getString(2);
			final String white = cursor.getString(3);
			final int drawoffer = cursor.getInt(4);

			final GameInfo info = new GameInfo(context, status, history, white, drawoffer);
			final Object[] data2 = {info.getYourTurn(), gameid};

			db.execSQL("UPDATE onlinegames SET yourturn=? WHERE gameid=?;", data2);
			cursor.moveToNext();
		}
		cursor.close();
	}

	/*
	 * Archive Game Queries
	 */

	public void insertArchiveGame(final JSONObject json)
	{
	try {
		final String gameid = json.getString("gameid");

		if (json.optBoolean("delete", false)) {
			deleteArchiveGame(gameid);
			return;
		}

		final int gametype = Enums.GameType(json.getString("gametype"));
		final int eventtype = Enums.EventType(json.getString("eventtype"));
		final int status = Enums.GameStatus(json.getString("status"));
		final long ctime = json.getLong("ctime");
		final long stime = json.getLong("stime");
		final String white = json.getString("white");
		final String black = json.getString("black");
		final String zfen = json.getString("zfen");
		final String history = json.getString("history");

		int w_psrfrom = 0;
		int w_psrto = 0;
		int b_psrfrom = 0;
		int b_psrto = 0;

		if (eventtype != Enums.INVITE) {
			w_psrfrom = json.getJSONObject("score").getJSONObject("white").getInt("from");
			w_psrto = json.getJSONObject("score").getJSONObject("white").getInt("to");
			b_psrfrom = json.getJSONObject("score").getJSONObject("black").getInt("from");
			b_psrto = json.getJSONObject("score").getJSONObject("black").getInt("to");
		}

		final String[] tmp = zfen.split(":");
		final int ply = Integer.parseInt(tmp[tmp.length - 1]);

		final Object[] data = {gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto,
			ctime, stime, ply, white, black, zfen, history};

		final String q1 = "INSERT OR REPLACE INTO archivegames ";
		final String q2 = "(gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto, ";
		final String q3 = "ctime, stime, ply, white, black, zfen, history) ";
		final String q4 = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		db.execSQL(q1 + q2 + q3 + q4, data);
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private boolean checkArchiveGame(final String gameid)
	{
		final String[] data = {gameid};
		return (db.rawQuery("SELECT gameid FROM archivegames WHERE gameid=?;", data).getCount() != 0);
	}

	public void deleteArchiveGame(final String gameid)
	{
		final Object[] data = {gameid};
		db.execSQL("DELETE FROM archivegames WHERE gameid=?;", data);
	}

	public List<String> getArchiveGameIds()
	{
		final String username = getUsername();
		final String[] data = {username, username};
		final SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM archivegames WHERE white=? OR black=?", data);
		final List<String> list = new ArrayList<>(cursor.getCount());

		cursor.moveToFirst();
		for (int i = 0, len = cursor.getCount(); i < len; i++) {
			list.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	public SQLiteCursor getArchiveGameList()
	{
		final String username = getUsername();
		final String[] data = {username, username};
		final String query = "SELECT * FROM archivegames LEFT JOIN " +
			"(SELECT gameid, unread FROM msgtable WHERE unread=1) USING(gameid) " +
			"WHERE white=? OR black=? GROUP BY gameid ORDER BY stime DESC";

		return (SQLiteCursor) db.rawQuery(query, data);
	}

	public void archiveNetworkGame(final String gameid, final int w_from, final int w_to, final int b_from, final int b_to)
	{
		final String[] data = {gameid};
		final Bundle row = rowToBundle(db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data), 0, true);

		final String tnames = "(gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto, ctime, stime, ply, white, black, zfen, history)";
		final String dstring = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		final Object[] data2 = {row.get("gameid"), row.get("gametype"), row.get("eventtype"), row.get("status"),
			w_from, w_to, b_from, b_to, row.get("ctime"), row.get("stime"), row.get("ply"),
			row.get("white"), row.get("black"), row.get("zfen"), row.get("history")};

		db.execSQL("INSERT OR REPLACE INTO archivegames " + tnames + " VALUES " + dstring + ';', data2);
		db.execSQL("DELETE FROM onlinegames WHERE gameid=?;", data);
	}

	/*
	 * Chat Queries
	 */

	public void insertMsg(final JSONObject json)
	{
	try {
		final String user = getUsername();
		final JSONArray players = json.getJSONArray("players");

		final String gameid = json.getString("gameid"),
			username = json.getString("username"),
			opponent = players.getString(username.equals(players.getString(0)) ? 1 : 0),
			msg = json.getString("txt");
		final long time = json.getLong("time");
		final int unread = (user.equals(username))? 0 : 1;

		final Object[] data = {gameid, time, username, msg, opponent, unread};
		db.execSQL("INSERT OR IGNORE INTO msgtable (gameid, time, username, msg, opponent, unread) VALUES (?, ?, ?, ?, ?, ?);", data);
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	public void setMsgsRead(final String gameid)
	{
		final Object[] data = {gameid};
		db.execSQL("UPDATE msgtable SET unread=0 WHERE gameid=?", data);
	}

	public void setAllMsgsRead()
	{
		final Object[] data = {};
		db.execSQL("UPDATE msgtable SET unread=0;", data);
	}

	public int getUnreadMsgCount(final String gameid)
	{
		final String[] data = {gameid};
		try (SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT COUNT(unread) FROM msgtable WHERE unread=1 AND gameid=?", data)) {
			cursor.moveToFirst();
			return cursor.getInt(0);
		}
	}

	public int getUnreadMsgCount()
	{
		final String username = getUsername();
		final String[] data = {username, username};
		final String query = "SELECT COUNT(*) FROM msgtable WHERE unread=1 AND (username=? OR opponent=?)";
		try (SQLiteCursor cursor = (SQLiteCursor) db.rawQuery(query, data)) {
			cursor.moveToFirst();
			return cursor.getInt(0);
		}
	}

	public long getNewestMsg()
	{
		final String username = getUsername();
		final String[] data = {username, username};

		try (SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT time FROM msgtable WHERE username=? OR opponent=? ORDER BY time DESC LIMIT 1", data)) {
			if (cursor.getCount() == 0)
				return 0;
			cursor.moveToFirst();
			return cursor.getLong(0);
		}
	}

	public SQLiteCursor getMsgList(final String gameid)
	{
		final String username = getUsername();
		final String[] data = {gameid, username, username};

		return (SQLiteCursor) db.rawQuery("SELECT * FROM msgtable WHERE gameid=? AND (username=? OR opponent=?) ORDER BY time ASC", data);
	}
}
