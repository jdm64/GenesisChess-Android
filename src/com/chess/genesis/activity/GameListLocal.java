package com.chess.genesis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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

public class GameListLocal extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener, OnTouchListener
{
	private Context context;
	private GameListAdapter gamelist_adapter;
	private Bundle settings;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case DeleteLocalDialog.MSG:
			case RenameGameDialog.MSG:
				gamelist_adapter.update();
				break;
			case NewLocalGameDialog.MSG:
				final GameDataDB db = new GameDataDB(context);
				final Bundle bundle = (Bundle) msg.obj;

				final int gametype2 = bundle.getInt("gametype");
				final int gameopp = bundle.getInt("opponent");
				String gamename = bundle.getString("name");
				if (gamename.length() < 1)
					gamename = "untitled";

				final Intent intent;
				if (gametype2 == Enums.GENESIS_CHESS)
					intent = new Intent(context, GenGame.class);
				else
					intent = new Intent(context, RegGame.class);
				intent.putExtras(db.newLocalGame(gamename, gametype2, gameopp));
				intent.putExtras(settings);
				db.close();

				startActivity(intent);
				break;
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(R.layout.activity_gamelist_local);

		// set click listeners
		ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		// set list adapters
		gamelist_adapter = new GameListAdapter(this, Enums.LOCAL_GAME, Enums.YOUR_TURN);

		final ListView gamelist_view = (ListView) findViewById(R.id.game_list);
		gamelist_view.setAdapter(gamelist_adapter);
		gamelist_view.setOnItemClickListener(this);

		// set empty view item
		final View empty = gamelist_adapter.getEmptyView(this);
		((ViewGroup) gamelist_view.getParent()).addView(empty);
		gamelist_view.setEmptyView(empty);

		registerForContextMenu(gamelist_view);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		gamelist_adapter.update();
	}

	@Override
	public void onDestroy()
	{
		gamelist_adapter.close();
		super.onDestroy();
	}

	public boolean onTouch(final View v, final MotionEvent event)
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

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.topbar_plus:
			(new NewLocalGameDialog(v.getContext(), handle)).show();
			break;
		}
	}

	public boolean onLongClick(final View v)
	{
		switch (v.getId()) {
		case R.id.topbar_genesis:
			finish();
			return true;
		default:
			return false;
		}
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
		getMenuInflater().inflate(R.menu.context_gamelist_local, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final Bundle bundle = (Bundle) gamelist_adapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			(new DeleteLocalDialog(this, handle, Integer.valueOf(bundle.getString("id")))).show();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(this, handle, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
}
