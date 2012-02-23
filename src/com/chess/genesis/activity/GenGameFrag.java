package com.chess.genesis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class GenGameFrag extends GameFrag implements OnClickListener
{
	public final static String TAG = "GAME";

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		// must be called before new GameState
		initBaseContentFrag();

		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getArguments();
		type = settings.getInt("type");

		// set view as black
		viewAsBlack = false;

		// create game state instance
		gamestate = new GenGameState(act, this, settings);

		// finalize initialization
		return super.onCreateView(inflater, container, settings);
	}

	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) act.findViewById(i);
			square.resetSquare();
		}
	}
}
