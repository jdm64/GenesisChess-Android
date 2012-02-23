package com.chess.genesis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameListOnlineFrag extends GameListFrag implements OnClickListener, OnTouchListener, OnItemClickListener
{
	public final static String TAG = "GAMELISTONLINE";

	private final static int THEIR_PAGE = 0;
	private final static int YOUR_PAGE = 1;
	private final static int ARCHIVE_PAGE = 2;

	private GameListAdapter[] gamelistadapter_arr;
	private NetworkClient net;
	private ProgressMsg progress;
	private ViewPager pager;

	public final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case DeleteArchiveDialog.MSG:
			case ReadAllMsgsDialog.MSG:
				updateGameList();
				break;
			case NewOnlineGameDialog.MSG:
				Bundle data = (Bundle) msg.obj;

				if (data.getInt("opponent") == Enums.INVITE) {
					(new InviteOptionsDialog(act, handle, data)).show();
					return;
				}
				progress.setText("Sending Newgame Request");
				String gametype = Enums.GameType(data.getInt("gametype"));

				net.join_game(gametype);
				(new Thread(net)).start();
				break;
			case RematchConfirm.MSG:
				data = (Bundle) msg.obj;
				progress.setText("Sending Newgame Request");

				final String opponent = data.getString("opp_name");
				String color = Enums.ColorType(data.getInt("color"));
				gametype = Enums.GameType(data.getInt("gametype"));

				net.new_game(opponent, gametype, color);
				(new Thread(net)).start();
				break;
			case NudgeConfirm.MSG:
				progress.setText("Sending Nudge");

				final String gameid = (String) msg.obj;
				net.nudge_game(gameid);
				(new Thread(net)).start();
				break;
			case InviteOptionsDialog.MSG:
				data = (Bundle) msg.obj;
				progress.setText("Sending Newgame Request");

				gametype = Enums.GameType(data.getInt("gametype"));
				color = Enums.ColorType(data.getInt("color"));

				net.new_game(data.getString("opp_name"), gametype, color);
				(new Thread(net)).start();
				break;
			case SyncClient.MSG:
			case NetworkClient.JOIN_GAME:
				JSONObject json = (JSONObject) msg.obj;
				try {
					if (json.getString("result").equals("error")) {
						progress.remove();
						Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
					if (msg.what == SyncClient.MSG || msg.what == NetworkClient.JOIN_GAME) {
						progress.setText("Checking Game Pool");
						updateGameList();
						GenesisNotifier.clearNotification(act, GenesisNotifier.YOURTURN_NOTE|GenesisNotifier.NEWMGS_NOTE);
						net.pool_info();
						(new Thread(net)).start();
					} else {
						progress.remove();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
				break;
			case NetworkClient.POOL_INFO:
				json = (JSONObject) msg.obj;
				try {
					if (json.getString("result").equals("error")) {
						progress.remove();
						Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
					final JSONArray games = json.getJSONArray("games");
					final Editor pref = PreferenceManager.getDefaultSharedPreferences(act).edit();
					pref.putString("poolinfo", games.toString());
					pref.commit();

					act.findViewById(R.id.game_search).setVisibility((games.length() == 0)? View.GONE : View.VISIBLE);

					progress.remove();
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
				break;
			case NetworkClient.NEW_GAME:
			case NetworkClient.NUDGE_GAME:
				json = (JSONObject) msg.obj;
				try {
					if (json.getString("result").equals("error")) {
						progress.remove();
						Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
					progress.setText("Updating Game List");
					final SyncClient sync = new SyncClient(act, handle);
					(new Thread(sync)).start();
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
				break;
			}
		}
	};

	private class GameListPager extends PagerAdapter
	{
		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public Object instantiateItem(final ViewGroup collection, final int position)
		{
			int type = Enums.ONLINE_GAME, yourmove = Enums.YOUR_TURN;

			switch (position) {
			case THEIR_PAGE:
				yourmove = Enums.THEIR_TURN;
				break;
			case YOUR_PAGE:
				// already initialized
				break;
			case ARCHIVE_PAGE:
				type = Enums.ARCHIVE_GAME;
				break;
			}
			final GameListAdapter list = new GameListAdapter(act, type, yourmove);
			gamelistadapter_arr[position] = list;

			final FrameLayout layout = (FrameLayout) act.getLayoutInflater().inflate(R.layout.gamelist_listview, null);
			final ListView listview = (ListView) layout.getChildAt(0);
			final View empty = list.getEmptyView(act);

			layout.addView(empty, 1);
			listview.setEmptyView(empty);
			listview.setAdapter(list);
			listview.setOnItemClickListener(GameListOnlineFrag.this);
			registerForContextMenu(listview);

			((ViewPager) collection).addView(layout, 0);

			return layout;
		}

		@Override
		public void destroyItem(final ViewGroup collection, final int position, final Object view)
		{
			gamelistadapter_arr[position].close();
			((ViewPager) collection).removeView((FrameLayout) view);
		}

		@Override
		public void startUpdate(final ViewGroup arg0)
		{
		}

		@Override
		public void finishUpdate(final ViewGroup arg0)
		{
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object)
		{
			return view == ((FrameLayout) object);
		}

		@Override
		public void restoreState(final Parcelable arg0, final ClassLoader arg1)
		{
		}

		@Override
		public Parcelable saveState()
		{
			return null;
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		initBaseContentFrag();

		final View view = inflater.inflate(R.layout.fragment_gamelist_online, container, false);

		gamelistadapter_arr = new GameListAdapter[3];
		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act);

	try {
		// Set "waiting for opponent"
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		final JSONArray pool = new JSONArray(pref.getString("poolinfo", "[]"));
		final View tpool = view.findViewById(R.id.game_search);
		tpool.setVisibility((pool.length() == 0)? View.GONE : View.VISIBLE);
		tpool.setOnClickListener(this);
		tpool.setOnTouchListener(this);
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}

		final SwipeTabsPagerAdapter tabAdapter = new SwipeTabsPagerAdapter(act, act.getSupportFragmentManager());
		tabAdapter.setTitles(new String[]{"Their Turn", "Your Turn", "Archive Games"});

		final SwipeTabs swipetabs = (SwipeTabs) view.findViewById(R.id.swipetabs);
		swipetabs.setAdapter(tabAdapter);

		pager = (ViewPager) view.findViewById(R.id.swipe_list);
		tabAdapter.setViewPager(pager);
		pager.setAdapter(new GameListPager());
		pager.setOnPageChangeListener(swipetabs);
		pager.setCurrentItem(YOUR_PAGE);

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
		(new Thread(sync)).start();
	}

	@Override
	public void onPause()
	{
		NetActive.dec();
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		for (int i = 0; i < 3; i++) {
			if (gamelistadapter_arr[i] != null)
				gamelistadapter_arr[i].close();
		}
		super.onDestroy();
	}

	public boolean onTouch(final View v, final MotionEvent event)
	{
		if (v.getId() == R.id.game_search) {
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				v.setBackgroundColor(0xff00b7eb);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				v.setBackgroundColor(0x00ffffff);
		}
		return false;
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.game_search)
			(new GamePoolDialog(v.getContext())).show();
		else if (v.getId() == R.id.menu)
			openMenu(v);
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Bundle data = (Bundle) parent.getItemAtPosition(position);

		loadGame(data);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = TAG;

		if (v.getId() == R.id.menu) {
			act.getMenuInflater().inflate(R.menu.options_gamelist_online, menu);
			return;
		}

		switch (pager.getCurrentItem()) {
		case THEIR_PAGE:
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			final GameListAdapter listAdapter = gamelistadapter_arr[pager.getCurrentItem()];
			final Bundle bundle = (Bundle) listAdapter.getItem((int) info.id);

			if (bundle.getString("idle").equals("1")) {
				act.getMenuInflater().inflate(R.menu.context_gamelist_online_nudge, menu);
				break;
			}
		case YOUR_PAGE:
			act.getMenuInflater().inflate(R.menu.context_gamelist_online, menu);
			break;
		case ARCHIVE_PAGE:
			act.getMenuInflater().inflate(R.menu.context_gamelist_archive, menu);
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (act.lastContextMenu != TAG)
			return super.onContextItemSelected(item);

		switch (item.getItemId()) {
		case R.id.new_game:
		case R.id.resync:
		case R.id.readall_msgs:
			return onOptionsItemSelected(item);
		}

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final GameListAdapter listAdapter = gamelistadapter_arr[pager.getCurrentItem()];
		final Bundle bundle = (Bundle) listAdapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			(new DeleteArchiveDialog(act, handle, bundle.getString("gameid"))).show();
			break;
		case R.id.local_copy:
			final int type = (pager.getCurrentItem() == ARCHIVE_PAGE)? Enums.ARCHIVE_GAME : Enums.ONLINE_GAME;
			(new CopyGameConfirm(act, bundle.getString("gameid"), type)).show();
			break;
		case R.id.rematch:
			final String username = listAdapter.getExtras().getString("username");
			final String opponent = username.equals(bundle.getString("white"))?
					bundle.getString("black") : bundle.getString("white");
			(new RematchConfirm(act, handle, opponent)).show();
			break;
		case R.id.nudge:
			(new NudgeConfirm(act, handle, bundle.getString("gameid"))).show();
			break;
		case R.id.share_game:
			sendGame(bundle);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.new_game:
			(new NewOnlineGameDialog(act, handle)).show();
			break;
		case R.id.resync:
			resyncList();
			break;
		case R.id.readall_msgs:
			(new ReadAllMsgsDialog(act, handle)).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void resyncList()
	{
		progress.setText("Updating Game List");

		final SyncClient sync = new SyncClient(act, handle);
		(new Thread(sync)).start();
	}

	public void updateGameList()
	{
		for (int i = 0; i < 3; i++) {
			if (gamelistadapter_arr[i] != null)
				gamelistadapter_arr[i].update();
		}
	}
}