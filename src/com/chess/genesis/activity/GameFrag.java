/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public abstract class GameFrag extends BaseContentFrag implements OnClickListener
{
	public final static String TAG = "GAME";

	public ViewFlip3D game_board;
	public GameState gamestate;
	public Bundle settings;
	public int type;
	public boolean viewAsBlack = false;

	protected GameListFrag gameListFrag;
	protected WakeLock wakelock;

	public final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case SubmitMove.MSG:
				if ((Boolean) msg.obj)
					gamestate.submitMove();
				else
					gamestate.undoMove();
				break;
			}
		}
	};

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_game, container, false);

		// initialize the board & place piece layouts
		final BoardLayout board = (BoardLayout) view.findViewById(R.id.board_layout);
		board.init(act, gamestate, viewAsBlack);
		final PlaceLayout place = (PlaceLayout) view.findViewById(R.id.place_layout);
		place.init(gamestate);

		// set click listeners
		if (type != Enums.LOCAL_GAME) {
			TabText txt = (TabText) view.findViewById(R.id.white_name);
			txt.setOnClickListener(this);
			txt = (TabText) view.findViewById(R.id.black_name);
			txt.setOnClickListener(this);
		}

		// set board nav click listeners
		final int list[] = new int[]{R.id.place_piece, R.id.backwards,
			R.id.forwards, R.id.current};
		for (int i = 0; i < list.length; i++) {
			final View button = act.findViewById(list[i]);
			button.setOnClickListener(this);
		}

		game_board = (ViewFlip3D) view.findViewById(R.id.board_flip);

		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// init board pieces
		gamestate.setBoard();

		gameListFrag = (GameListFrag) fragMan.findFragmentById(R.id.panel01);
	}

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

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		if (pref.getBoolean("screenAlwaysOn", false)) {
			final PowerManager pm = (PowerManager) act.getSystemService(Context.POWER_SERVICE);
			wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "GenesisChess");
			wakelock.acquire();
		}
	}

	@Override
	public void onPause()
	{
		gamestate.save(act, true);

		if (type == Enums.ONLINE_GAME)
			NetActive.dec();
		if (wakelock != null)
			wakelock.release();

		if (isTablet)
			gameListFrag.updateGameList();
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
			if (isTablet) {
				final MsgBoxFrag frag = new MsgBoxFrag();
				frag.setArguments(settings);

				fragMan.beginTransaction()
				.replace(R.id.panel03, frag, MsgBoxFrag.TAG)
				.addToBackStack(MsgBoxFrag.TAG).commit();
			} else {
				intent = new Intent(act, MsgBox.class);
				intent.putExtra("gameid", settings.getString("gameid"));
				startActivity(intent);
			}
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
			showUserStats(settings.getString("white"));
			break;
		case R.id.black_name:
			showUserStats(settings.getString("black"));
			break;
		case R.id.menu:
			openMenu(v);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = TAG;

		switch (type) {
		case Enums.LOCAL_GAME:
			act.getMenuInflater().inflate(R.menu.options_game_local, menu);
			break;
		case Enums.ONLINE_GAME:
			if (Integer.valueOf(settings.getString("ply")) > 58)
				act.getMenuInflater().inflate(R.menu.options_game_online_draw, menu);
			else
				act.getMenuInflater().inflate(R.menu.options_game_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			act.getMenuInflater().inflate(R.menu.options_game_archive, menu);
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (act.lastContextMenu.equals(TAG))
			return onOptionsItemSelected(item);
		else
			return super.onContextItemSelected(item);
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
		case R.id.local_details:
			(new LocalGameDetails(act, gamestate.getBundle())).show();
			break;
		case R.id.online_details:
			(new OnlineGameDetails(act, gamestate.getBundle())).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	protected void showUserStats(final String username)
	{
		if (isTablet) {
			// Pop all non-game frags
			fragMan.popBackStack(GameFrag.TAG, 0);

			final UserStatsFrag frag = new UserStatsFrag();
			final MenuBarFrag menubar = new MenuBarFrag();
			final Bundle bundle = new Bundle();

			bundle.putString("username", username);
			frag.setArguments(bundle);
			frag.setMenuBarFrag(menubar);

			fragMan.beginTransaction()
			.replace(R.id.topbar03, menubar, MenuBarFrag.TAG)
			.replace(R.id.panel03, frag, UserStatsFrag.TAG)
			.addToBackStack(UserStatsFrag.TAG).commit();
		} else {
			final Intent intent = new Intent(act, UserStats.class);
			intent.putExtra("username", username);
			startActivity(intent);
		}
	}

	public void displaySubmitMove()
	{
		final SubmitMove dialog = new SubmitMove(act, handle, isTablet);
		dialog.show();
	}

	public void reset()
	{
		for (int i = 994; i < 1000; i++) {
			final PlaceButton piece = (PlaceButton) act.findViewById(i);
			piece.reset();
		}
		for (int i = 1001; i < 1007; i++) {
			final PlaceButton piece = (PlaceButton) act.findViewById(i);
			piece.reset();
		}
		gamestate.setStm();
	}
}
