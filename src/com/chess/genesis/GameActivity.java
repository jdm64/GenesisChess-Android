package com.chess.genesis;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class GameActivity extends Activity
{
	private static final int TAKEBACK_MOVE = 1;
	private static final int FORWARD_MOVE = 2;
	private static final int RESET_GAME = 3;

	public static GameActivity self;

	public GameState gamestate;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// set content view
		GameLayout gamelayout = new GameLayout(this);
		setContentView(gamelayout);

		// create new game
		gamestate = new GameState(getIntent().getExtras(), gamelayout);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		gamestate.save(this, true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, TAKEBACK_MOVE, Menu.NONE, "Take Back Move");
		menu.add(Menu.NONE, FORWARD_MOVE, Menu.NONE, "Forward Move");
		menu.add(Menu.NONE, RESET_GAME, Menu.NONE, "Reset Game");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case TAKEBACK_MOVE:
			gamestate.backMove();
			break;
		case FORWARD_MOVE:
			gamestate.forwardMove();
			break;
		case RESET_GAME:
			gamestate.reset();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
