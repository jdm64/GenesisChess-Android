package com.chess.genesis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends Activity implements OnClickListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(R.layout.mainmenu);

		// setup click listeners
		Button button = (Button) findViewById(R.id.local_game);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.online_game);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.archive_game);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.login);
		button.setOnClickListener(this);
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);
	}

	public void onClick(View v)
	{
		Bundle bundle;
		Intent intent;

		switch (v.getId()) {
		case R.id.local_game:
			bundle = new Bundle();
			bundle.putInt("type", Enums.LOCAL_GAME);

			intent = new Intent(this, GameList.class);
			intent.putExtras(bundle);

			startActivity(intent);
			break;
		case R.id.online_game:
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

			if (!settings.getBoolean("isLoggedIn", false)) {
				Toast.makeText(getApplication(), "You must login\nbefore playing online", Toast.LENGTH_LONG).show();
				return;
			}
			bundle = new Bundle();
			bundle.putInt("type", Enums.ONLINE_GAME);

			intent = new Intent(this, GameList.class);
			intent.putExtras(bundle);

			startActivity(intent);
			break;
		case R.id.archive_game:
			bundle = new Bundle();
			bundle.putInt("type", Enums.ARCHIVE_GAME);

			intent = new Intent(this, GameList.class);
			intent.putExtras(bundle);

			startActivity(intent);
			break;
		case R.id.login:
			startActivity(new Intent(this, Login.class));
			break;
		}
	}
}
