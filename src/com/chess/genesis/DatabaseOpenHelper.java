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
		"type TEXT," +
		"opponent TEXT," +
		"name TEXT DEFAULT 'Untitled'," +
		"zfen TEXT," +
		"history TEXT);";

/*
	private static final String ONLINE_GAME_CREATE_TABEL =
		"CREATE TABLE onlinegames (" +
		"id INT PRIMARY KEY," +
		"time INT," +
		"whiteuser TEXT," +
		"blackuser TEXT," +
		"zfen TEXT," +
		"movehistory TEXT," +
		
*/

	DatabaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(LOCAL_GAME_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}