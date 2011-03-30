package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends Activity implements OnTouchListener, OnClickListener, OnLongClickListener
{
	private static Login self;

	private NetworkClient net;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;

			try {
				if (json.getString("result").equals("error")) {
					Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return;
				}
				switch (msg.what) {
				case NetworkClient.LOGIN:
					Toast.makeText(self, json.getString("reason"), Toast.LENGTH_LONG).show();

					SocketClient.isLoggedin = true;

					EditText txt = (EditText) findViewById(R.id.username);
					final String username = txt.getText().toString();

					txt = (EditText) findViewById(R.id.password);
					final String password = txt.getText().toString();

					Editor pref = PreferenceManager.getDefaultSharedPreferences(self).edit();
					pref.putBoolean("isLoggedIn", true);
					pref.putString("username", username);
					pref.putString("passhash", password);
					pref.commit();

					final SyncGameList sync = new SyncGameList(self, handle, username);
					sync.setFullSync(true);
					(new Thread(sync)).start();

					Toast.makeText(self, "Syncing active games", Toast.LENGTH_LONG).show();
					break;
				case SyncGameList.MSG:
					Toast.makeText(self, "Syncing complete", Toast.LENGTH_LONG).show();
					setResult(RESULT_OK);
					finish();
					break;
				case LogoutConfirm.MSG:
					pref = PreferenceManager.getDefaultSharedPreferences(self).edit();

					pref.putBoolean("isLoggedIn", false);
					pref.putString("username", "!error!");
					pref.putString("passhash", "!error!");
					pref.commit();

					txt = (EditText) findViewById(R.id.username);
					txt.setText("");

					txt = (EditText) findViewById(R.id.password);
					txt.setText("");
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException();
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

		// create network client instance
		net = new NetworkClient(this, handle);

		// set content view
		setContentView(R.layout.login);

		// setup click listeners
		ImageView image = (ImageView) findViewById(R.id.login);
		image.setOnTouchListener(this);
		image.setOnClickListener(this);

		image = (ImageView) findViewById(R.id.register);
		image.setOnTouchListener(this);
		image.setOnClickListener(this);

		image = (ImageView) findViewById(R.id.topbar);
		image.setOnTouchListener(this);
		image.setOnLongClickListener(this);

		// Always show the currently logged in user
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean("isLoggedIn", false)) {
			EditText txt = (EditText) findViewById(R.id.username);
			txt.setText(pref.getString("username", ""));

			txt = (EditText) findViewById(R.id.password);
			txt.setText("");
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		NetActive.inc();
	}

	@Override
	public void onPause()
	{
		NetActive.dec();
		super.onPause();
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.login:
			EditText txt = (EditText) findViewById(R.id.username);
			final String username = txt.getText().toString();

			txt = (EditText) findViewById(R.id.password);
			final String password = txt.getText().toString();

			net.login_user(username, password);
			(new Thread(net)).start();
			Toast.makeText(this, "Connecting to server...", Toast.LENGTH_LONG).show();
			break;
		case R.id.register:
			startActivityForResult(new Intent(this, Register.class), 1);
			break;
		}
	}

	public boolean onLongClick(final View v)
	{
		switch (v.getId()) {
		case R.id.topbar:
			finish();
			return true;
		default:
			return false;
		}
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
		case R.id.login:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.login_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.login);
			break;
		case R.id.register:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.register_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.register);
			break;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.login_options, menu);
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

	public void onActivityResult(final int reques, final int result, final Intent data)
	{
		String username = "";
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (pref.getBoolean("isLoggedIn", false))
			username = pref.getString("username", "");

		EditText txt = (EditText) findViewById(R.id.username);
		txt.setText(username);

		txt = (EditText) findViewById(R.id.password);
		txt.setText("");
	}
}
