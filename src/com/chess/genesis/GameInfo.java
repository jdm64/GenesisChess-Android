package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class GameInfo
{
	private String history;
	private String white;
	private String black;

	private Context context;

	public GameInfo(Context _context, String History, String White, String Black)
	{
		history = History;
		white = White;
		black = Black;
		context = _context;
	}

	public int getPly()
	{
		if (history == null || history.length() < 3)
			return 0;
		String[] count = history.trim().split(" +");
		return count.length;
	}

	public int getYourTurn()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		String color = (getPly() % 2 == 0)? white : black;

		return color.equals(settings.getString("username", "!error!"))? 1:0;
	}
}