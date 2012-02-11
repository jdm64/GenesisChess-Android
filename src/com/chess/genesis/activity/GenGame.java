package com.chess.genesis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class GenGame extends Game implements OnClickListener, OnLongClickListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// set view as black
		viewAsBlack = false;

		// create game state instance
		gamestate = new GenGameState(this, this, settings);

		// finalize initialization
		init();
	}

	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) findViewById(i);
			square.resetSquare();
		}
	}
}
