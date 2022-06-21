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
import android.view.*;
import android.view.View.*;
import android.widget.AdapterView.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import androidx.fragment.app.*;
import androidx.viewpager.widget.*;

public class GameListOnlineFrag extends GameListFrag implements OnTouchListener, OnItemClickListener, OnClickListener
{
	private final Handler handle = new Handler(this);
	private NetworkClient net;
	private ProgressMsg progress;
	private ViewPager pager;

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what) {
		case DeleteArchiveDialog.MSG:
		case ReadAllMsgsDialog.MSG:
			updateGameList();
			break;
		case NewOnlineGameDialog.MSG:
			Bundle data = (Bundle) msg.obj;

			if (data.getInt("opponent") == Enums.INVITE) {
				new InviteOptionsDialog(act, handle, data).show();
				return true;
			}
			progress.setText("Sending Newgame Request");
			String gametype = Enums.GameType(data.getInt("gametype"));

			net.join_game(gametype);
			new Thread(net).start();
			break;
		case RematchConfirm.MSG:
			data = (Bundle) msg.obj;
			progress.setText("Sending Newgame Request");

			final String opponent = data.getString("opp_name");
			String color = Enums.ColorType(data.getInt("color"));
			gametype = Enums.GameType(data.getInt("gametype"));

			net.new_game(opponent, gametype, color);
			new Thread(net).start();
			break;
		case NudgeConfirm.MSG:
			progress.setText("Sending Nudge");

			final String gameid = (String) msg.obj;
			net.nudge_game(gameid);
			new Thread(net).start();
			break;
		case InviteOptionsDialog.MSG:
			data = (Bundle) msg.obj;
			progress.setText("Sending Newgame Request");

			gametype = Enums.GameType(data.getInt("gametype"));
			color = Enums.ColorType(data.getInt("color"));

			net.new_game(data.getString("opp_name"), gametype, color);
			new Thread(net).start();
			break;
		case SyncClient.MSG:
		case NetworkClient.JOIN_GAME:
			JSONObject json = (JSONObject) msg.obj;
			try {
				if (json.getString("result").equals("error")) {
					progress.dismiss();
					Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return true;
				}
				if (msg.what == SyncClient.MSG || msg.what == NetworkClient.JOIN_GAME) {
					progress.setText("Checking Game Pool");
					updateGameList();
					GenesisNotifier.clearNotification(act, GenesisNotifier.YOURTURN_NOTE|GenesisNotifier.NEWMGS_NOTE);
					net.pool_info();
					new Thread(net).start();
				} else {
					progress.dismiss();
				}
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			break;
		case NetworkClient.POOL_INFO:
			json = (JSONObject) msg.obj;
			try {
				if (json.getString("result").equals("error")) {
					progress.dismiss();
					Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return true;
				}
				final JSONArray games = json.getJSONArray("games");
				new PrefEdit(act)
					.putString(R.array.pf_poolinfo, games.toString())
					.commit();

				act.findViewById(R.id.game_search).setVisibility((games.length() == 0)? View.GONE : View.VISIBLE);

				progress.dismiss();
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			break;
		case NetworkClient.NEW_GAME:
		case NetworkClient.NUDGE_GAME:
			json = (JSONObject) msg.obj;
			try {
				if (json.getString("result").equals("error")) {
					progress.dismiss();
					Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return true;
				}
				progress.setText("Updating Game List");
				final SyncClient sync = new SyncClient(act, handle);
				new Thread(sync).start();
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			break;
		}
		return true;
	}

	@Override
	public void onCreate(final Bundle data)
	{
		super.onCreate(data);
		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_gamelist_online, container, false);

	try {
		// Set "waiting for opponent"
		final JSONArray pool = new JSONArray(Pref.getString(act, R.array.pf_poolinfo));
		final View tpool = view.findViewById(R.id.game_search);

		tpool.setVisibility((pool.length() == 0)? View.GONE : View.VISIBLE);
		tpool.setOnClickListener(this);
		tpool.setOnTouchListener(this);
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}

		PagerTabStrip tabs = view.findViewById(R.id.pager_title_strip);
		tabs.setTabIndicatorColorResource(R.color.blue_light_500);

		pager = view.findViewById(R.id.swipe_list);
		pager.setAdapter(new GameListPager(getFragmentManager()));
		pager.setCurrentItem(Enums.YOUR_PAGE);

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateGameList();

		// start background notifier
		act.startService(new Intent(act, GenesisNotifier.class));

		NetActive.inc();
		progress.setText("Updating Game List");

		final SyncClient sync = new SyncClient(act, handle);
		new Thread(sync).start();
	}

	@Override
	public void onPause()
	{
		NetActive.dec(getActivity());
		super.onPause();
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event)
	{
		if (v.getId() == R.id.game_search)
			v.setBackgroundColor((event.getAction() == MotionEvent.ACTION_DOWN)? MColors.BLUE_LIGHT_500 : MColors.CLEAR);
		return false;
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.game_search)
			new GamePoolDialog(v.getContext()).show();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_gamelist_online, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.new_game:
			new NewOnlineGameDialog(act, handle).show();
			break;
		case R.id.resync:
			resyncList();
			break;
		case R.id.readall_msgs:
			new ReadAllMsgsDialog(act, handle).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void resyncList()
	{
		progress.setText("Updating Game List");

		final SyncClient sync = new SyncClient(act, handle);
		new Thread(sync).start();
	}

	@Override
	public void updateGameList()
	{
		final FragmentPagerAdapter adapter = (FragmentPagerAdapter) pager.getAdapter();
		for (int i = 0; i < adapter.getCount(); i++)
			((GameListPage) adapter.getItem(i)).update();
	}
}
