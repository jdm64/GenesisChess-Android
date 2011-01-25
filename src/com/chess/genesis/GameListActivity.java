package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;

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
}
