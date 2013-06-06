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

package com.chess.genesis.activity;

import android.content.*;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.preference.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import com.chess.genesis.view.*;

public abstract class GameFrag extends BaseContentFrag
{
	protected final static String TAG = "GAME";

	public ViewAnimator game_board;
	public CapturedLayout captured_count;
	public GameState gamestate;
	public int type;
	public boolean viewAsBlack = false;

	protected GameListFrag gameListFrag;
	protected WakeLock wakelock;

	public final Handler handle = new Handler()
	{
		@Override
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
	public String getBTag()
	{
		return TAG;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_game, container, false);

		// initialize the board & place piece layouts
		final BoardLayout board = (BoardLayout) view.findViewById(R.id.board_layout);
		board.init(gamestate, viewAsBlack);
		final PlaceLayout place = (PlaceLayout) view.findViewById(R.id.place_layout);
		place.init(gamestate);

		// set click listeners
		type = getArguments().getInt("type");
		if (type != Enums.LOCAL_GAME) {
			TabText txt = (TabText) view.findViewById(R.id.white_name);
			txt.setOnClickListener(this);
			txt = (TabText) view.findViewById(R.id.black_name);
			txt.setOnClickListener(this);
		}

		// set board nav click listeners
		final int list[] = new int[]{R.id.place_piece, R.id.backwards,
			R.id.forwards, R.id.current};
		for (final int element : list) {
			final View button = act.findViewById(element);
			button.setOnClickListener(this);
		}

		game_board = (ViewAnimator) view.findViewById(R.id.board_flip);
		captured_count = (CapturedLayout) view.findViewById(R.id.captured_count);

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
		if (pref.getBoolean(PrefKey.SCREEN_ALWAYS_ON, false)) {
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

	@Override
	public void onClick(final View v)
	{
		final Bundle settings = getArguments();
		Intent intent;

		switch (v.getId()) {
		case R.id.place_piece:
			AnimationFactory.flipTransition(game_board);
			break;
		case R.id.chat:
			if (isTablet) {
				final MsgBoxFrag frag = new MsgBoxFrag();
				frag.setArguments(settings);

				fragMan.beginTransaction()
				.replace(R.id.panel03, frag, frag.getBTag())
				.addToBackStack(frag.getBTag()).commit();
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
		act.lastContextMenu = getBTag();

		switch (type) {
		case Enums.LOCAL_GAME:
			act.getMenuInflater().inflate(R.menu.options_game_local, menu);
			break;
		case Enums.ONLINE_GAME:
			act.getMenuInflater().inflate(R.menu.options_game_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			act.getMenuInflater().inflate(R.menu.options_game_archive, menu);
			break;
		}
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
			new GameDetailsDialog(act, gamestate.getBundle(), false).show();
			break;
		case R.id.online_details:
			new GameDetailsDialog(act, gamestate.getBundle(), true).show();
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
			.replace(R.id.topbar03, menubar, getBTag())
			.replace(R.id.panel03, frag, frag.getBTag())
			.addToBackStack(frag.getBTag()).commit();
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
