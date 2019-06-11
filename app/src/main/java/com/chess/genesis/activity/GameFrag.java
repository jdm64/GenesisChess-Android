/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.activity;

import android.content.*;
import android.os.*;
import android.os.Handler.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import com.chess.genesis.view.*;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.*;

public abstract class GameFrag extends AbstractActivityFrag implements Callback, ISqLocator, IGameFrag, OnClickListener, OnMenuItemClickListener
{
	private DrawerLayout2 boardDrawer;
	private CapturedLayout captured_count;
	GameState gamestate;
	int type;
	boolean viewAsBlack = false;
	private GameListFrag gameListFrag;
	private BoardView board;

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what) {
		case SubmitMove.MSG:
			if ((Boolean) msg.obj)
				gamestate.submitMove();
			else
				gamestate.undoMove();
			break;
		}
		return true;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		setKeepScreenOn();

		final View view = inflater.inflate(R.layout.fragment_game, container, false);

		// initialize the board & place piece layouts
		board = view.findViewById(R.id.board_layout);
		board.init(gamestate, viewAsBlack);
		final PlaceLayout place = view.findViewById(R.id.place_layout);
		place.init(gamestate);

		// set click listeners
		type = getArguments().getInt("type");
		if (type != Enums.LOCAL_GAME) {
			TabText txt = view.findViewById(R.id.white_name);
			txt.setOnClickListener(this);
			txt = view.findViewById(R.id.black_name);
			txt.setOnClickListener(this);
		}

		Toolbar navBar = view.findViewById(R.id.board_nav);
		navBar.inflateMenu(R.menu.options_nav);
		navBar.setOnMenuItemClickListener(this);

		if (Integer.parseInt(getArguments().getString("gametype")) == Enums.GENESIS_CHESS) {
			navBar.setLogo(R.drawable.placepiece);
			navBar.setOnClickListener(this);
		}

		boardDrawer = view.findViewById(R.id.board_drawer);
		captured_count = view.findViewById(R.id.captured_count);

		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// init board pieces
		gamestate.setBoard();
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
	}

	@Override
	public void onPause()
	{
		gamestate.save(act, true);

		if (type == Enums.ONLINE_GAME)
			NetActive.dec(getActivity());

		super.onPause();
	}

	@Override
	public void onClick(final View v)
	{
		final Bundle settings = getArguments();

		switch (v.getId()) {
		case R.id.board_nav:
			boardDrawer.toggle(Gravity.LEFT);
			break;
		case R.id.white_name:
			showUserStats(settings.getString("white"));
			break;
		case R.id.black_name:
			showUserStats(settings.getString("black"));
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			inflater.inflate(R.menu.options_game_local, menu);
			break;
		case Enums.ONLINE_GAME:
			inflater.inflate(R.menu.options_game_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			inflater.inflate(R.menu.options_game_archive, menu);
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
		case R.id.game_details:
			GameDetailsDialog.create(gamestate.getBundle(), type != Enums.LOCAL_GAME).show(getFragmentManager(), "");
			break;
		case R.id.chat:
			Bundle settings = getArguments();
			Intent intent;

			intent = new Intent(act, MsgBox.class);
			intent.putExtra("gameid", settings.getString("gameid"));
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		switch (item.getItemId()) {
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
		return true;
	}

	private void showUserStats(final String username)
	{
		final Intent intent = new Intent(act, UserStats.class);
		intent.putExtra("username", username);
		startActivity(intent);
	}

	private void setKeepScreenOn()
	{
		if (Pref.getBool(act, R.array.pf_screenAlwaysOn)) {
			act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public ISquare getSq(int index)
	{
		return index < 0x88? getBoardSq(index) : getPlaceSq(index);
	}

	@Override
	public IBoardSq getBoardSq(int index)
	{
		return board.getSquare(index);
	}

	@Override
	public IPlaceSq getPlaceSq(int index)
	{
		return (IPlaceSq) act.findViewById(index);
	}

	@Override
	public void setCapturedCounts(int[] counts)
	{
		captured_count.setPieces(counts);
	}

	@Override
	public void showSubmitMove()
	{
		new SubmitMove(act, new Handler(this)).show();
	}

	@Override
	public void togglePlaceBoard()
	{
		boardDrawer.toggle(Gravity.LEFT);
	}

	@Override
	public Bundle getGameData()
	{
		return getArguments();
	}

	@Override
	public void showToast(String text)
	{
		Toast.makeText(act, text, Toast.LENGTH_LONG).show();
	}

	@Override
	public void setNameText(boolean isWhite, boolean isStm, boolean isMate, String text)
	{
		final TabText tabtext = act.findViewById(isWhite? R.id.white_name : R.id.black_name);
		tabtext.setText(text);
		tabtext.setActive(isStm);
		if (isMate)
			tabtext.setTabTextColor(MColors.RED_A700);
	}
}
