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
		"gameid TEXT PRIMARY KEY," +
		"gametype INTEGER," +
		"ctime INTEGER," +
		"stime INTEGER DEFAULT 0," +
		"yourturn INTEGER DEFAULT 0," + // 1 = your turn, 0 = opponent's turn
		"ply INTEGER DEFAULT 0," +
		"white TEXT," +
		"black TEXT," +
		"zfen TEXT DEFAULT ' '," +
		"history TEXT DEFAULT ' ');";

	private final static String MSG_CREATE_TABLE =
		"CREATE TABLE msgtable (" +
		"id INTEGER PRIMARY KEY ASC, " +
		"gameid TEXT," +
		"time INTEGER," +
		"username TEXT," +
		"msg TEXT);";

	DatabaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(LOCAL_GAME_CREATE_TABLE);
		db.execSQL(NETWORK_GAME_CREATE_TABLE);
		db.execSQL(MSG_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}