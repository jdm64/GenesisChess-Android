package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

public class GameList extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener, OnTouchListener
{
	private ListView gamelist_view;

	public GameListAdapter gamelist_adapter;

	public static GameList self;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// set content view
		setContentView(R.layout.gamelist);

		// set click listeners
		ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnTouchListener(this);
		button.setOnClickListener(this);

		gamelist_adapter = new GameListAdapter(this);

		gamelist_view = (ListView) findViewById(R.id.game_list);
		gamelist_view.setAdapter(gamelist_adapter);
		gamelist_view.setOnItemClickListener(this);

		registerForContextMenu(gamelist_view);
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
			GameDataDB db = new GameDataDB(v.getContext());
			Intent intent = new Intent(this, Game.class);

			intent.putExtras(db.newLocalGame("genesis", "local"));
			db.close();
			startActivityForResult(intent, 1);
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
			db.deleteLocalGame(bundle.getString("id"));
			db.close();
			gamelist_adapter.update();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(this, bundle.getString("id"), bundle.getString("name"))).show();
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
