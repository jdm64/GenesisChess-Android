package com.chess.genesis;

import android.view.View;

class GameState
{
	public static GameState self;

	public void boardClick(final View v)
	{
		self.boardClick(v);
	}

	public void placeClick(final View v)
	{
		self.placeClick(v);
	}
}
