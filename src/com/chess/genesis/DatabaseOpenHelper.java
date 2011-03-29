package com.chess.genesis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseOpenHelper extends SQLiteOpenHelper
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

	private static final String ONLINE_GAME_CREATE_TABLE =
		"CREATE TABLE onlinegames (" +
		"gameid TEXT PRIMARY KEY," +
		"gametype INTEGER," +
		"eventtype INTEGER DEFAULT 1," +
		"status INTEGER," +
		"ctime INTEGER," +
		"stime INTEGER DEFAULT 0," +
		"yourturn INTEGER DEFAULT 0," + // 1 = your turn, 0 = opponent's turn
		"ply INTEGER DEFAULT 0," +
		"white TEXT," +
		"black TEXT," +
		"zfen TEXT DEFAULT ' '," +
		"history TEXT DEFAULT ' ');";

	private static final String ARCHIVE_GAME_CREATE_TABLE =
		"CREATE TABLE archivegames (" +
		"gameid TEXT PRIMARY KEY," +
		"gametype INTEGER," +
		"eventtype INTEGER DEFAULT 1," + // 1 = random game
		"status INTEGER," +
		"w_psrfrom INTEGER," +
		"w_psrto INTEGER," +
		"b_psrfrom INTEGER," +
		"b_psrto INTEGER," +
		"ctime INTEGER," +
		"stime INTEGER DEFAULT 0," +
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

	public DatabaseOpenHelper(final Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db)
	{
		db.execSQL(LOCAL_GAME_CREATE_TABLE);
		db.execSQL(ONLINE_GAME_CREATE_TABLE);
		db.execSQL(MSG_CREATE_TABLE);
		db.execSQL(ARCHIVE_GAME_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
	{
	}
}
