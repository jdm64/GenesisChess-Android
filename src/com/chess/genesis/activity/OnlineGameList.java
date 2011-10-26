package com.chess.genesis;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class OnlineGameList extends FragmentActivity implements OnClickListener, OnLongClickListener, OnTouchListener, OnItemClickListener
{
	public static OnlineGameList self;

	private final static int THEIR_PAGE = 0;
	private final static int YOUR_PAGE = 1;
	private final static int ARCHIVE_PAGE = 2;

	private GameListAdapter[] gamelistadapter_arr;
	private NetworkClient net;
	private ProgressMsg progress;
	private ViewPager pager;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case NewOnlineGameDialog.MSG:
				Bundle data = (Bundle) msg.obj;

				if (data.getInt("opponent") == Enums.INVITE) {
					(new InviteOptionsDialog(self, handle, data)).show();
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
			case InviteOptionsDialog.MSG:
				data = (Bundle) msg.obj;
				progress.setText("Sending Newgame Request");

				gametype = Enums.GameType(data.getInt("gametype"));
				color = Enums.ColorType(data.getInt("color"));

				net.new_game(data.getString("opp_name"), gametype, color);
				(new Thread(net)).start();
				break;
			case SyncGameList.MSG:
			case NetworkClient.JOIN_GAME:
			case NetworkClient.NEW_GAME:
				final JSONObject json = (JSONObject) msg.obj;
				try {
					if (json.getString("result").equals("error")) {
						progress.remove();
						Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
					progress.remove();

					if (msg.what == SyncGameList.MSG) {
						self.updateGameListAdapters();
						if (gamelistadapter_arr[YOUR_PAGE].getCount() == 0) {
							final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
							nm.cancelAll();
						}
					}
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
		public Object instantiateItem(final View collection, final int position)
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
			final GameListAdapter list = new GameListAdapter(self, type, yourmove);
			gamelistadapter_arr[position] = list;

			final ListView view = (ListView) getLayoutInflater().inflate(R.layout.gamelist_listview, null);
			view.setAdapter(list);
			view.setOnItemClickListener(self);
			self.registerForContextMenu(view);

			// set empty view item
			final View empty = list.getEmptyView(self);
			((ViewGroup) pager.getParent()).addView(empty);
			view.setEmptyView(empty);

			((ViewPager) collection).addView(view, 0);
			return view;
		}

		@Override
		public void destroyItem(final View collection, final int position, final Object view)
		{
			final GameListAdapter list = (GameListAdapter) ((ListView) view).getAdapter();
			list.close();

			((ViewPager) collection).removeView((ListView) view);
		}

		@Override
		public void startUpdate(final View arg0)
		{
		}

		@Override
		public void finishUpdate(final View arg0)
		{
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object)
		{
			return view == ((ListView) object);
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
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;
		gamelistadapter_arr = new GameListAdapter[3];

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		net = new NetworkClient(this, handle);
		progress = new ProgressMsg(this);

		// set content view
		setContentView(R.layout.gamelist_swipe);

		ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		final SwipeTabsPagerAdapter tabAdapter = new SwipeTabsPagerAdapter(this, getSupportFragmentManager());
		tabAdapter.setTitles(new String[]{"Their Turn", "Your Turn", "Archive Games"});

		final SwipeTabs swipetabs = (SwipeTabs) findViewById(R.id.swipetabs);
		swipetabs.setAdapter(tabAdapter);

		pager = (ViewPager) findViewById(R.id.swipe_list);
		tabAdapter.setViewPager(pager);
		pager.setAdapter(new GameListPager());
		pager.setOnPageChangeListener(swipetabs);
		pager.setCurrentItem(YOUR_PAGE);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateGameListAdapters();

		// start background notifier
		startService(new Intent(this, GenesisNotifier.class));

		NetActive.inc();
		progress.setText("Updating Game List");

		// Must not be final
		SyncGameList sync = new SyncGameList(this, handle);
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
		for (int i = 0; i < 3; i++)
			gamelistadapter_arr[i].close();
		super.onDestroy();
	}

	public boolean onTouch(final View v, final MotionEvent event)
	{
		switch (v.getId()) {
		case R.id.topbar:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.topbar_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.topbar);
			break;
		case R.id.topbar_genesis:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.topbar_genesis_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.topbar_genesis);
			break;
		case R.id.topbar_plus:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.topbar_plus_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.topbar_plus);
			break;
		}
		return false;
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.topbar_plus:
			(new NewOnlineGameDialog(v.getContext(), handle)).show();
			break;
		}
	}

	public boolean onLongClick(final View v)
	{
		switch (v.getId()) {
		case R.id.topbar:
		case R.id.topbar_genesis:
			finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.gamelist_options_online, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.resync:
			progress.setText("Updating Game List");

			// Must not be final
			SyncGameList sync = new SyncGameList(this, handle);
			(new Thread(sync)).start();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Bundle data = (Bundle) parent.getItemAtPosition(position);
		final Intent intent;

		if (Integer.valueOf(data.getString("gametype")) == Enums.GENESIS_CHESS)
			intent = new Intent(this, GenGame.class);
		else
			intent = new Intent(this, RegGame.class);

		intent.putExtras(data);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		switch (pager.getCurrentItem()) {
		case THEIR_PAGE:
		case YOUR_PAGE:
			getMenuInflater().inflate(R.menu.gamelist_context_online, menu);
			break;
		case ARCHIVE_PAGE:
			getMenuInflater().inflate(R.menu.gamelist_context_archive, menu);
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final GameListAdapter listAdapter = gamelistadapter_arr[pager.getCurrentItem()];
		final Bundle bundle = (Bundle) listAdapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			(new DeleteArchiveDialog(this, bundle.getString("gameid"))).show();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(this, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
			break;
		case R.id.local_copy:
			final int type = (pager.getCurrentItem() == ARCHIVE_PAGE)? Enums.ARCHIVE_GAME : Enums.ONLINE_GAME;
			(new CopyGameConfirm(this, bundle.getString("gameid"), type)).show();
			break;
		case R.id.rematch:
			final String username = listAdapter.getExtras().getString("username");
			final String opponent = username.equals(bundle.getString("white"))?
					bundle.getString("black") : bundle.getString("white");
			(new RematchConfirm(this, handle, opponent)).show();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void updateGameListAdapters()
	{
		for (int i = 0; i < 3; i++) {
			if (gamelistadapter_arr[i] != null)
				gamelistadapter_arr[i].update();
		}
	}
}
