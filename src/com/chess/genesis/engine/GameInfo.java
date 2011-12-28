package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class GameInfo
{
	private final Context context;
	private final String history;
	private final String white;
	private final int status;
	private final int draw;

	public GameInfo(final Context _context, final int Status, final String History, final String White, final int Draw)
	{
		history = History;
		white = White;
		status = Status;
		context = _context;
		draw = Draw;
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
			return Enums.YOUR_TURN;

		final int color = white.equals(pref.getString("username", "!error!"))? Piece.WHITE : Piece.BLACK;

		if (draw != 0)
			return (color * draw > 0)? Enums.THEIR_TURN : Enums.YOUR_TURN;
		else if (status != Enums.ACTIVE)
			return Enums.YOUR_TURN;

		final int stm = (getPly() % 2 == 0)? Piece.WHITE : Piece.BLACK;
		return (stm == color)? Enums.YOUR_TURN : Enums.THEIR_TURN;
	}
}
