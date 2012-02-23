package com.chess.genesis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class RegGameFrag extends GameFrag implements OnClickListener
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

		// set playingBlack
		boolean playingBlack = false;
		if (type != Enums.LOCAL_GAME) {
			playingBlack = settings.getString("username").equals(settings.getString("black"));
		} else {
			final int oppType = Integer.valueOf(settings.getString("opponent"));
			playingBlack = (oppType == Enums.CPU_WHITE_OPPONENT)? true : false;
		}

		// set view as black
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		viewAsBlack = pref.getBoolean("viewAsBlack", true)? playingBlack : false;

		// create game stat instance
		gamestate = new RegGameState(act, this, settings);

		// finalize initialization
		return super.onCreateView(inflater, container, settings);
	}

	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) act.findViewById(i);
			square.setPiece(Piece.EMPTY);
		}
		for (int i = 0; i < 32; i++) {
			final int loc = BaseBoard.EE64(RegBoard.InitRegPiece[i]);
			final BoardButton square = (BoardButton) act.findViewById(loc);
			square.setPiece(Move.InitPieceType[i]);
		}
	}
}
