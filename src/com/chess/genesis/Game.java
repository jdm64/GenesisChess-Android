package com.chess.genesis;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class Game extends Activity implements OnClickListener, OnLongClickListener, OnTouchListener
{
	public static Game self;

	public static ViewFlip3D game_board;
	public static TextView stm_txt;

	private int type;

	private GameState gamestate;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(R.layout.game_layout);

		// set click listeners
		ImageView button = (ImageView) findViewById(R.id.topbar);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		button = (ImageView) findViewById(R.id.place_piece);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		// initialize variables
		stm_txt = (TextView) findViewById(R.id.stm_txt);
		game_board = (ViewFlip3D) findViewById(R.id.board_flip);

		// create gamestate instance
		Bundle bundle = getIntent().getExtras();
		type = bundle.getInt("type");
		gamestate = new GameState(this, bundle);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		gamestate.save(this, true);
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.place_piece:
			game_board.flip();
			break;
		}
	}

	public boolean onLongClick(View v)
	{
		switch (v.getId()) {
		case R.id.topbar:
			gamestate.save(this, true);
			finish();
			return true;
		default:
			return false;
		}
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		switch (v.getId()) {
		case R.id.place_piece:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.place_piece_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.place_piece);
			break;
		case R.id.topbar:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.topbar_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.topbar);
			break;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (type == Enums.ONLINE_GAME)
			getMenuInflater().inflate(R.menu.game_options_online, menu);
		else
			getMenuInflater().inflate(R.menu.game_options_local, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.takeback_move:
			gamestate.backMove();
			break;
		case R.id.forward_move:
			gamestate.forwardMove();
			break;
		case R.id.reset_game:
			gamestate.reset();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void reset()
	{
		for (int i = 0; i < 64; i++) {
			BoardButton square = (BoardButton) findViewById(i);
			square.resetSquare();
		}
		for (int i = 94; i < 100; i++) {
			PlaceButton piece = (PlaceButton) findViewById(i);
			piece.reset();
		}
		for (int i = 101; i < 107; i++) {
			PlaceButton piece = (PlaceButton) findViewById(i);
			piece.reset();
		}
		stm_txt.setText("White's Turn");
	}
}
