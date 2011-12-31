package com.chess.genesis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public abstract class Game extends Activity
{
	public ViewFlip3D game_board;
	public boolean viewAsBlack = false;

	protected WakeLock wakelock;
	protected GameState gamestate;
	protected Bundle settings;
	protected int type;
	protected boolean newMsgs = false;

	public abstract void reset();

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState)
	{
		savedInstanceState.putAll(gamestate.getBundle());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (type == Enums.ONLINE_GAME)
			NetActive.inc();
		if (type != Enums.LOCAL_GAME) {
			final GameDataDB db = new GameDataDB(this);
			final int count = db.getUnreadMsgCount(settings.getString("gameid"));
			final int img = (count > 0)? R.drawable.btn_newmsg : R.drawable.btn_chat;

			newMsgs = (count > 0);

			final ImageView v = (ImageView) findViewById(R.id.chat);
			((ImageView) v).setImageResource(img);
			db.close();
		}

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean("screenAlwaysOn", false)) {
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "GenesisChess");
			wakelock.acquire();
		}
	}

	@Override
	public void onPause()
	{
		gamestate.save(this, true);
		if (type == Enums.ONLINE_GAME)
			NetActive.dec();

		if (wakelock != null)
			wakelock.release();

		super.onPause();
	}

	public void onClick(final View v)
	{
		Intent intent;

		switch (v.getId()) {
		case R.id.place_piece:
			game_board.flip();
			break;
		case R.id.chat:
			intent = new Intent(this, MsgBox.class);
			intent.putExtra("gameid", settings.getString("gameid"));
			startActivity(intent);
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
		case R.id.white_name:
			intent = new Intent(this, UserStats.class);
			intent.putExtra("username", settings.getString("white"));
			startActivity(intent);
			break;
		case R.id.black_name:
			intent = new Intent(this, UserStats.class);
			intent.putExtra("username", settings.getString("black"));
			startActivity(intent);
			break;
		}
	}

	public boolean onLongClick(final View v)
	{
		switch (v.getId()) {
		case R.id.topbar:
		case R.id.topbar_genesis:
			gamestate.save(this, true);
			finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			getMenuInflater().inflate(R.menu.options_game_local, menu);
			break;
		case Enums.ONLINE_GAME:
			if (Integer.valueOf(settings.getString("ply")) > 58)
				getMenuInflater().inflate(R.menu.options_game_online_draw, menu);
			else
				getMenuInflater().inflate(R.menu.options_game_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			getMenuInflater().inflate(R.menu.options_game_archive, menu);
			break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.first_move:
			gamestate.firstMove();
			break;
		case R.id.resync:
			gamestate.resync();
			break;
		case R.id.nudge_resign:
			gamestate.nudge_resign();
			break;
		case R.id.rematch:
			gamestate.rematch();
			break;
		case R.id.draw:
			gamestate.draw();
			break;
		case R.id.cpu_time:
			gamestate.setCpuTime();
			break;
		case R.id.chat:
			final Intent intent = new Intent(this, MsgBox.class);
			intent.putExtra("gameid", settings.getString("gameid"));
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void onActivityResult(final int reques, final int result, final Intent data)
	{
		if (result == RESULT_OK)
			gamestate.submitMove();
		else if (result == RESULT_CANCELED)
			gamestate.undoMove();
	}

	public void displaySubmitMove()
	{
		startActivityForResult(new Intent(this, SubmitMove.class), 1);
	}
}
