package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "gamedata";

	private static final String LOCAL_GAME_CREATE_TABLE =
		"CREATE TABLE localgames (" +
		"id INTEGER PRIMARY KEY ASC, " +
		"ctime INTEGER," +
		"stime INTEGER," +
		"gametype INTEGER," +
		"opponent INTEGER," +
		"name TEXT DEFAULT 'Untitled'," +
		"zfen TEXT," +
		"history TEXT);";

	private static final String NETWORK_GAME_CREATE_TABLE =
		"CREATE TABLE onlinegames (" +
		"gameid STRING PRIMARY KEY," +
		"gametype INTEGER," +
		"ctime INTEGER," +
		"stime INTEGER," +
		"yourturn INTEGER," + // 1 = your turn, 0 = opponent's turn
		"ply INTEGER," +
		"white TEXT," +
		"black TEXT," +
		"zfen TEXT," +
		"history TEXT);";

	DatabaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(LOCAL_GAME_CREATE_TABLE);
		db.execSQL(NETWORK_GAME_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}