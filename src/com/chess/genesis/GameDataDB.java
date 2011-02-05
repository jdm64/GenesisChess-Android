package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import java.util.Date;

public class GameDataDB
{
	private SQLiteDatabase db;

	public GameDataDB(Context context)
	{
		db = (new DatabaseOpenHelper(context)).getWritableDatabase();
	}

	public void close()
	{
		db.close();
	}

	public static Bundle rowToBundle(SQLiteCursor cursor, int index)
	{
		Bundle bundle = new Bundle();
		String[] column = cursor.getColumnNames();

		cursor.moveToPosition(index);
		for (int i = 0; i < cursor.getColumnCount(); i++)
			bundle.putString(column[i], cursor.getString(i));
		return bundle;
	}

	public Bundle newLocalGame(int gametype, int opponent)
	{
		long time = (new Date()).getTime();
		Object[] data = {time, time, gametype, opponent};
		String[] data2 = {String.valueOf(time)};

		db.execSQL("INSERT INTO localgames (ctime, stime, gametype, opponent) VALUES (?, ?, ?, ?);", data);
		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM localgames WHERE ctime=?", data2);

		return rowToBundle(cursor, 0);
	}

	public void saveLocalGame(int id, long stime, String zfen, String history)
	{
		Object[] data = {stime, zfen, history, id};
		db.execSQL("UPDATE localgames SET stime=?, zfen=?, history=? WHERE id=?;", data);
	}

	public void deleteLocalGame(int id)
	{
		Object[] data = {id};
		db.execSQL("DELETE FROM localgames WHERE id=?;", data);
	}

	public void renameLocalGame(int id, String name)
	{
		Object[] data = {name, id};
		db.execSQL("UPDATE localgames SET name=? WHERE id=?;", data);
	}

	public SQLiteCursor getLocalGameList()
	{
		return (SQLiteCursor) db.rawQuery("SELECT * FROM localgames ORDER BY stime DESC", null);
	}

	public SQLiteCursor getOnlineGameList()
	{
		return (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames ORDER BY yourturn DESC, stime DESC", null);
	}

}