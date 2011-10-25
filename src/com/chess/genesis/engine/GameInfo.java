package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class GameInfo
{
	private final Context context;
	private final String history;
	private final String white;
	private final String black;
	private final int status;

	public GameInfo(final Context _context, final int Status, final String History, final String White, final String Black)
	{
		history = History;
		white = White;
		black = Black;
		status = Status;
		context = _context;
	}

	public int getPly()
	{
		if (history == null || history.length() < 3)
			return 0;
		return history.trim().split(" +").length;
	}

	public int getYourTurn()
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		if (status != Enums.ACTIVE)
			return 1;
		final String color = (getPly() % 2 == 0)? white : black;
		return color.equals(pref.getString("username", "!error!"))? 1:0;
	}
}
