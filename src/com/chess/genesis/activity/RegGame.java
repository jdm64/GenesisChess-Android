package com.chess.genesis;

import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class RegGame extends Game implements OnClickListener, OnLongClickListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getIntent().getExtras();
		type = settings.getInt("type");

		// set playingBlack
		boolean playingBlack = false;
		if (type != Enums.LOCAL_GAME) {
			playingBlack = settings.getString("username").equals(settings.getString("black"));
		} else {
			final int oppType = Integer.valueOf(settings.getString("opponent"));
			playingBlack = (oppType == Enums.CPU_WHITE_OPPONENT)? true : false;
		}

		// set view as black
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		viewAsBlack = pref.getBoolean("viewAsBlack", true)? playingBlack : false;

		// create game stat instance
		gamestate = new RegGameState(this, settings);

		// finalize initialization
		init();
	}

	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) findViewById(i);
			square.setPiece(RegBoard.InitRegBoard[i]);
		}
	}
}
