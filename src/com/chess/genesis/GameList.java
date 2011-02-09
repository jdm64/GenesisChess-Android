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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class GameList extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener, OnTouchListener
{
	private ListView gamelist_view;

	private Bundle settings;

	public GameListAdapter gamelist_adapter;

	public static GameList self;

	private NetworkClient net;

	private Handler handle = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case NewOnlineGameDialog.MSG:

				Bundle data = (Bundle) msg.obj;

				String username = settings.getString("username");
				String gametype;

				switch (data.getInt("gametype")) {
				default:
				case Enums.GENESIS_CHESS:
					gametype = "genesis";
					break;
				case Enums.REGULAR_CHESS:
					gametype = "regular";
					break;
				}
				if (data.getInt("opponent") == Enums.RANDOM)
					net.join_game(username, gametype);
				else
					net.new_game(username, gametype, data.getString("opp_name"));

				(new Thread(net)).start();
				Toast.makeText(getApplication(), "Connecting to server...", Toast.LENGTH_LONG).show();
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

		// Remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// store settings from main menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		settings = getIntent().getExtras();
		settings.putString("username", prefs.getString("username", "!error!"));

		net = new NetworkClient(handle);

		// set content view
		setContentView(R.layout.gamelist);

		// set click listeners
		ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		gamelist_adapter = new GameListAdapter(this, settings);

		gamelist_view = (ListView) findViewById(R.id.game_list);
		gamelist_view.setAdapter(gamelist_adapter);
		gamelist_view.setOnItemClickListener(this);

		registerForContextMenu(gamelist_view);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (settings.getInt("type", Enums.ONLINE_GAME) == Enums.ONLINE_GAME) {
			SyncGameList sync = new SyncGameList(this, handle, settings.getString("username"));
			(new Thread(sync)).start();
			Toast.makeText(getApplication(), "Updating game list...", Toast.LENGTH_LONG).show();
		}
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

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.topbar_plus:
			Intent intent = new Intent(this, Game.class);

			if (settings.getInt("type") == Enums.LOCAL_GAME) {
				GameDataDB db = new GameDataDB(v.getContext());

				intent.putExtras(db.newLocalGame(Enums.GENESIS_CHESS, Enums.HUMAN_OPPONENT));
				db.close();
				startActivityForResult(intent, 1);
			} else {
				(new NewOnlineGameDialog(v.getContext(), handle)).show();
			}
			break;
		}
	}

	public boolean onLongClick(View v)
	{
		switch (v.getId()) {
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
		startActivityForResult(intent, 1);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		getMenuInflater().inflate(R.menu.gamelist_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Bundle bundle = (Bundle) gamelist_adapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			GameDataDB db = new GameDataDB(this);
			db.deleteLocalGame(Integer.valueOf(bundle.getString("id")));
			db.close();
			gamelist_adapter.update();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(this, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	public void onActivityResult(int request, int result, Intent data)
	{
		gamelist_adapter.update();
	}
}
