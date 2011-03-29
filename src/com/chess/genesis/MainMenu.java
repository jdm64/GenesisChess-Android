package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends Activity implements OnClickListener
{
	private static MainMenu self;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case LogoutConfirm.MSG:
				final Editor settings = PreferenceManager.getDefaultSharedPreferences(self).edit();

				settings.putBoolean("isLoggedIn", false);
				settings.putString("username", "!error!");
				settings.putString("passhash", "!error!");
				settings.commit();

				final TextView text = (TextView) findViewById(R.id.welcome);
				text.setText("");

				final Button button = (Button) findViewById(R.id.login);
				button.setVisibility(View.VISIBLE);
				break;
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;

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

		button = (Button) findViewById(R.id.howtoplay);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.likefacebook);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.login);
		button.setOnClickListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		String welcome = "";
		int visible = View.VISIBLE;

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.getBoolean("isLoggedIn", false)) {
			welcome = "Welcome " + settings.getString("username", "");
			visible = View.GONE;
		}
		final TextView text = (TextView) findViewById(R.id.welcome);
		text.setText(welcome);

		final Button button = (Button) findViewById(R.id.login);
		button.setVisibility(visible);
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.mainmenu_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.logout:
			(new LogoutConfirm(this, handle)).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (resultCode == RESULT_OK) {
			final Bundle bundle = new Bundle();
			bundle.putInt("type", Enums.ONLINE_GAME);

			final Intent intent = new Intent(this, GameList.class);
			intent.putExtras(bundle);

			startActivity(intent);
		}
	}

	public void onClick(final View v)
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
			final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

			if (!settings.getBoolean("isLoggedIn", false)) {
				startActivityForResult(new Intent(this, Login.class), 1);
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
		case R.id.howtoplay:
			Uri uri = Uri.parse("http://goo.gl/eyLYY");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.likefacebook:
			uri = Uri.parse("http://goo.gl/tQVOh");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.login:
			startActivity(new Intent(this, Login.class));
			break;
		}
	}
}
