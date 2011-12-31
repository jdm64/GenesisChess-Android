package com.chess.genesis;

import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

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

		// set content view
		if (type != Enums.LOCAL_GAME)
			setContentView(R.layout.activity_game_online);
		else
			setContentView(R.layout.activity_game_local);

		// must be called after setContentView
		gamestate = new RegGameState(this, settings);

		// initialize the board & place piece
		final BoardLayout board = (BoardLayout) findViewById(R.id.board_layout);
		board.init(this, gamestate);
		final PlaceLayout place = (PlaceLayout) findViewById(R.id.place_layout);
		place.init(gamestate);

		// init board pieces
		gamestate.setBoard();

		// set click listeners
		ImageView button = (ImageView) findViewById((type != Enums.LOCAL_GAME)? R.id.topbar_genesis : R.id.topbar);
		button.setOnLongClickListener(this);

		if (type != Enums.LOCAL_GAME) {
			button = (ImageView) findViewById(R.id.chat);
			button.setOnClickListener(this);

			TabText txt = (TabText) findViewById(R.id.white_name);
			txt.setOnClickListener(this);
			txt = (TabText) findViewById(R.id.black_name);
			txt.setOnClickListener(this);
		}

		final int list[] = new int[]{R.id.place_piece, R.id.backwards,
			R.id.forwards, R.id.current};
		for (int i = 0; i < list.length; i++) {
			button = (ImageView) findViewById(list[i]);
			button.setOnClickListener(this);
		}

		game_board = (ViewFlip3D) findViewById(R.id.board_flip);
	}

	public void reset()
	{
		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) findViewById(i);
			square.setPiece(RegBoard.InitRegBoard[i]);
		}
		for (int i = 94; i < 100; i++) {
			final PlaceButton piece = (PlaceButton) findViewById(i);
			piece.reset();
		}
		for (int i = 101; i < 107; i++) {
			final PlaceButton piece = (PlaceButton) findViewById(i);
			piece.reset();
		}
		gamestate.setStm();
	}
}
