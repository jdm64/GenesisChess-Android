package com.chess.genesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends BasePhoneActivity implements OnClickListener
{
	private Context context;
	private NetworkClient net;
	private ProgressMsg progress;
	private boolean exitActivity = false;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case NetworkClient.LOGIN:
			case SyncClient.MSG:
				handleNetwork(msg);
				break;
			case LogoutConfirm.MSG:
				final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();

				pref.putBoolean("isLoggedIn", false);
				pref.putString("username", "!error!");
				pref.putString("passhash", "!error!");
				pref.putLong("lastgamesync", 0);
				pref.putLong("lastmsgsync", 0);
				pref.commit();

				EditText txt = (EditText) findViewById(R.id.username);
				txt.setText("");

				txt = (EditText) findViewById(R.id.password);
				txt.setText("");
				break;
			case ProgressMsg.MSG:
				if (exitActivity)
					finish();
				break;
			}
		}

		private void handleNetwork(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;

		try {
			if (json.getString("result").equals("error")) {
				exitActivity = false;
				progress.remove();
				Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

			switch (msg.what) {
			case NetworkClient.LOGIN:
				progress.setText("Syncing Data");

				EditText txt = (EditText) findViewById(R.id.username);
				final String username = txt.getText().toString().trim();

				txt = (EditText) findViewById(R.id.password);
				final String password = txt.getText().toString();

				final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
				pref.putBoolean("isLoggedIn", true);
				pref.putString("username", username);
				pref.putString("passhash", password);
				pref.commit();

				SocketClient.getInstance().setIsLoggedIn(true);

				final SyncClient sync = new SyncClient(context, handle);
				sync.setSyncType(SyncClient.FULL_SYNC);
				(new Thread(sync)).start();
				break;
			case SyncClient.MSG:
				// start background notifier
				startService(new Intent(context, GenesisNotifier.class));

				final GameDataDB db = new GameDataDB(context);
				db.recalcYourTurn();
				db.close();

				exitActivity = true;
				setResult(RESULT_OK);
				progress.dismiss();
				break;
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, R.layout.activity_login);
		context = this;

		// create network client instance
		net = new NetworkClient(this, handle);
		progress = new ProgressMsg(this, handle);

		// setup click listeners
		ImageView image = (ImageView) findViewById(R.id.login);
		image.setOnClickListener(this);
		image = (ImageView) findViewById(R.id.register);
		image.setOnClickListener(this);

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
		AdsHandler.run(this);
	}

	@Override
	public void onPause()
	{
		NetActive.dec();
		super.onPause();
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.login) {
			progress.setText("Requesting Login");

			EditText txt = (EditText) findViewById(R.id.username);
			final String username = txt.getText().toString().trim();

			txt = (EditText) findViewById(R.id.password);
			final String password = txt.getText().toString();

			net.login_user(username, password);
			(new Thread(net)).start();
		} else if (v.getId() == R.id.register) {
			startActivityForResult(new Intent(this, Register.class), 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.logout) {
			(new LogoutConfirm(this, handle)).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
