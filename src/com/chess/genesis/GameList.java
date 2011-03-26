package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class GameList extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener, OnTouchListener
{
	public static GameList self;

	public GameListAdapter gamelist_adapter;

	private int type;
	private boolean yourmove;

	private Bundle settings;
	private NetworkClient net;

	private Handler handle = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case NewOnlineGameDialog.MSG:
				Bundle data = (Bundle) msg.obj;

				if (data.getInt("opponent") == Enums.INVITE) {
					(new InviteOptionsDialog(self, handle, data)).show();
					return;
				}
				String username = settings.getString("username");
				String gametype = Enums.GameType(data.getInt("gametype"));

				net.join_game(username, gametype);
				(new Thread(net)).start();
				Toast.makeText(getApplication(), "Connecting to server...", Toast.LENGTH_LONG).show();
				break;
			case InviteOptionsDialog.MSG:
				data = (Bundle) msg.obj;

				username = settings.getString("username");
				gametype = Enums.GameType(data.getInt("gametype"));
				String color = Enums.ColorType(data.getInt("color"));

				net.new_game(username, data.getString("opp_name"), gametype, color);
				(new Thread(net)).start();
				Toast.makeText(getApplication(), "Connecting to server...", Toast.LENGTH_LONG).show();
				break;
			case NewLocalGameDialog.MSG:
				GameDataDB db = new GameDataDB(self);
				Bundle bundle = (Bundle) msg.obj;

				int gametype2 = bundle.getInt("gametype");
				int gameopp = bundle.getInt("opponent");
				String gamename = bundle.getString("name");
				if (gamename.length() < 1)
					gamename = "untitled";

				Intent intent = new Intent(self, Game.class);
				intent.putExtras(db.newLocalGame(gamename, gametype2, gameopp));
				intent.putExtras(settings);
				db.close();

				startActivity(intent);
				break;
			case SyncGameList.MSG:
			case NetworkClient.JOIN_GAME:
			case NetworkClient.NEW_GAME:
				JSONObject json = (JSONObject) msg.obj;
				try {
					if (json.getString("result").equals("error")) {
						Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
					Toast.makeText(getApplication(), json.getString("reason"), Toast.LENGTH_LONG).show();

					if (msg.what == SyncGameList.MSG)
						gamelist_adapter.update();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// store settings from main menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		settings = getIntent().getExtras();
		settings.putString("username", prefs.getString("username", "!error!"));
		type = settings.getInt("type", Enums.ONLINE_GAME);
		yourmove = true;

		net = new NetworkClient(this, handle);

		// set content view
		switch (type) {
		case Enums.ONLINE_GAME:
			setContentView(R.layout.gamelist_online);

			ImageView button = (ImageView) findViewById(R.id.your_move);
			button.setOnTouchListener(this);
			button.setOnClickListener(this);

			button = (ImageView) findViewById(R.id.their_move);
			button.setOnTouchListener(this);
			button.setOnClickListener(this);
			break;
		case Enums.LOCAL_GAME:
		default:
			setContentView(R.layout.gamelist_local);
			break;
		case Enums.ARCHIVE_GAME:
			setContentView(R.layout.gamelist_archive);
			break;
		}

		// set click listeners
		switch (type) {
		case Enums.ONLINE_GAME:
		case Enums.LOCAL_GAME:
		default:
			ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
			button.setOnTouchListener(this);
			button.setOnLongClickListener(this);

			button = (ImageView) findViewById(R.id.topbar_plus);
			button.setOnTouchListener(this);
			button.setOnClickListener(this);
			break;
		case Enums.ARCHIVE_GAME:
			button = (ImageView) findViewById(R.id.topbar);
			button.setOnTouchListener(this);
			button.setOnLongClickListener(this);
			break;
		}

		// set list adapters
		gamelist_adapter = new GameListAdapter(this, settings);

		ListView gamelist_view = (ListView) findViewById(R.id.game_list);
		gamelist_view.setAdapter(gamelist_adapter);
		gamelist_view.setOnItemClickListener(this);

		registerForContextMenu(gamelist_view);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (settings.getInt("type", Enums.ONLINE_GAME) == Enums.ONLINE_GAME) {
			NetActive.inc();

			SyncGameList sync = new SyncGameList(this, handle, settings.getString("username"));
			(new Thread(sync)).start();
			Toast.makeText(getApplication(), "Updating game list...", Toast.LENGTH_LONG).show();
		} else {
			gamelist_adapter.update();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if (settings.getInt("type") == Enums.ONLINE_GAME)
			NetActive.dec();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		gamelist_adapter.close();
	}

	public boolean onTouch(View v, MotionEvent event)
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
		case R.id.your_move:
			if (yourmove) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					((ImageView) v).setImageResource(R.drawable.yourmove_selected_pressed);
				else if (event.getAction() == MotionEvent.ACTION_UP)
					((ImageView) v).setImageResource(R.drawable.yourmove_selected);
			} else {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					((ImageView) v).setImageResource(R.drawable.yourmove_notselected_pressed);
				else if (event.getAction() == MotionEvent.ACTION_UP)
					((ImageView) v).setImageResource(R.drawable.yourmove_notselected);
			}
			break;
		case R.id.their_move:
			if (yourmove) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					((ImageView) v).setImageResource(R.drawable.theirmove_notselected_pressed);
				else if (event.getAction() == MotionEvent.ACTION_UP)
					((ImageView) v).setImageResource(R.drawable.theirmove_notselected);
			} else {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					((ImageView) v).setImageResource(R.drawable.theirmove_selected_pressed);
				else if (event.getAction() == MotionEvent.ACTION_UP)
					((ImageView) v).setImageResource(R.drawable.theirmove_selected);
			}
			break;
		}
		return false;
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.topbar_plus:
			Intent intent = new Intent(this, Game.class);

			if (settings.getInt("type") == Enums.LOCAL_GAME)
				(new NewLocalGameDialog(v.getContext(), handle)).show();
			else
				(new NewOnlineGameDialog(v.getContext(), handle)).show();
			break;
		case R.id.your_move:
			if (yourmove)
				return;
			yourmove = true;
			gamelist_adapter.setYourturn(1);

			ImageView button = (ImageView) findViewById(R.id.your_move);
			button.setImageResource(R.drawable.yourmove_selected);

			button = (ImageView) findViewById(R.id.their_move);
			button.setImageResource(R.drawable.theirmove_notselected);
			break;
		case R.id.their_move:
			if (!yourmove)
				return;
			yourmove = false;
			gamelist_adapter.setYourturn(0);

			button = (ImageView) findViewById(R.id.your_move);
			button.setImageResource(R.drawable.yourmove_notselected);

			button = (ImageView) findViewById(R.id.their_move);
			button.setImageResource(R.drawable.theirmove_selected);
			break;
		}
	}

	public boolean onLongClick(View v)
	{
		switch (v.getId()) {
		case R.id.topbar:
			finish();
			return true;
		case R.id.topbar_genesis:
			finish();
			return true;
		default:
			return false;
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Bundle data = (Bundle) parent.getItemAtPosition(position);
		Intent intent = new Intent(this, Game.class);

		intent.putExtras(data);
		intent.putExtras(settings);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		switch (type) {
		case Enums.LOCAL_GAME:
			getMenuInflater().inflate(R.menu.gamelist_context_local, menu);
			break;
		case Enums.ONLINE_GAME:
			getMenuInflater().inflate(R.menu.gamelist_context_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			getMenuInflater().inflate(R.menu.gamelist_context_archive, menu);
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Bundle bundle = (Bundle) gamelist_adapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			if (type == Enums.LOCAL_GAME)
				(new DeleteLocalDialog(this, Integer.valueOf(bundle.getString("id")))).show();
			else if (type == Enums.ARCHIVE_GAME)
				(new DeleteArchiveDialog(this, bundle.getString("gameid"))).show();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(this, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (type == Enums.ONLINE_GAME)
			getMenuInflater().inflate(R.menu.gamelist_options_online, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.resync:
			SyncGameList sync = new SyncGameList(this, handle, settings.getString("username"));
			(new Thread(sync)).start();
			Toast.makeText(getApplication(), "Updating game list...", Toast.LENGTH_LONG).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
