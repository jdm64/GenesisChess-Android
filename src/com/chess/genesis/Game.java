package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
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
		setContentView(R.layout.game);

		// set click listeners
		ImageView button = (ImageView) findViewById(R.id.topbar);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		button = (ImageView) findViewById(R.id.place_piece);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		button = (ImageView) findViewById(R.id.backwards);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		button = (ImageView) findViewById(R.id.forwards);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		button = (ImageView) findViewById(R.id.current);
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
	public void onResume()
	{
		super.onResume();

		if (type == Enums.ONLINE_GAME)
			NetActive.inc();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		gamestate.save(this, true);
		if (type == Enums.ONLINE_GAME)
			NetActive.dec();
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.place_piece:
			game_board.flip();
			break;
		case R.id.backwards:
			gamestate.backMove();
			break;
		case R.id.forwards:
			gamestate.forwardMove();
			break;
		case R.id.current:
			gamestate.currentMove();
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
		case R.id.backwards:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.backwards_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.backwards);
			break;
		case R.id.forwards:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.forwards_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.forwards);
			break;
		case R.id.current:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.current_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.current);
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
		case R.id.first_move:
			gamestate.firstMove();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void onActivityResult(int reques, int result, Intent data)
	{
		switch (result) {
		case RESULT_OK:
			gamestate.submitMove();
			break;
		case RESULT_CANCELED:
			gamestate.undoMove();
			break;
		}
	}

	public void displaySubmitMove()
	{
		startActivityForResult(new Intent(self, SubmitMove.class), 1);
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
