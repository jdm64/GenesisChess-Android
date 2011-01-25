package com.chess.genesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView.BufferType;

public class GameListActivity extends Activity implements OnClickListener
{
	public static GameListActivity self;

	private GameListLayout view;

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
		view = new GameListLayout(this);
		setContentView(view);

		registerForContextMenu(view.gamelistview);
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case GameListLayout.NEW_GAME:
			GameDataDB db = new GameDataDB(v.getContext());
			Intent intent = new Intent(this, GameActivity.class);

			intent.putExtras(db.newLocalGame("genesis", "local"));
			db.close();			
			startActivityForResult(intent, 1);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(Menu.NONE, 1, Menu.NONE, "Delete Game");
		menu.add(Menu.NONE, 2, Menu.NONE, "Rename Game");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Bundle bundle = (Bundle) view.gamelist.getItem((int) info.id);

		switch (item.getItemId()) {
		case 1:
			Log.d("id", bundle.getString("id"));
			GameDataDB db = new GameDataDB(this);
			db.deleteLocalGame(bundle.getString("id"));
			db.close();
			view.updateList();
			break;
		case 2:
			(new RenameGame(this, bundle.getString("id"), bundle.getString("name"))).show();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	public void onActivityResult(int request, int result, Intent data)
	{
		view.updateList();
	}

	public void selectGame(AdapterView<?> parent, int position)
	{
		Bundle data = (Bundle) parent.getItemAtPosition(position);
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtras(data);
		startActivityForResult(intent, 1);
	}

	private class RenameGame extends AlertDialog implements OnClickListener
	{
		private EditText txtinput;
		private String gameid;
		private String gamename;

		public RenameGame(Context context, String id, String name)
		{
			super(context);

			gameid = id;
			gamename = name;
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			setTitle("Rename Game");

			LinearLayout cont = new LinearLayout(GameListActivity.self);
			cont.setOrientation(LinearLayout.VERTICAL);

			LinearLayout layout = new LinearLayout(GameListActivity.self);

			txtinput = new EditText(GameListActivity.self);
			txtinput.setText(gamename, BufferType.EDITABLE);
			layout.addView(txtinput);
			cont.addView(layout);

			layout = new LinearLayout(GameListActivity.self);

			Button button = new Button(GameListActivity.self);
			button.setText("Save");
			button.setId(1);
			button.setOnClickListener(this);
			layout.addView(button);

			button = new Button(GameListActivity.self);
			button.setText("Cancel");
			button.setId(2);
			button.setOnClickListener(this);
			layout.addView(button);

			cont.addView(layout);

			setContentView(cont);
		}

		public void onClick(View v)
		{
			if (v.getId() == 1) {
				GameDataDB db = new GameDataDB(GameListActivity.self);
				db.renameLocalGame(gameid, txtinput.getText().toString());
				db.close();
				view.updateList();
			}
			dismiss();
		}
	}
}
