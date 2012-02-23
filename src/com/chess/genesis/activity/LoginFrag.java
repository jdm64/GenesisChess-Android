package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFrag extends BaseContentFrag implements OnClickListener
{
	public final static String TAG = "LOGIN";

	private NetworkClient net;
	private ProgressMsg progress;
	private int callbackId = Enums.NO_ACTIVITY;
	private boolean exitActivity = false;

	public final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case NetworkClient.LOGIN:
			case SyncClient.MSG:
				handleNetwork(msg);
				break;
			case LogoutConfirm.MSG:
				final Editor pref = PreferenceManager.getDefaultSharedPreferences(act).edit();

				pref.putBoolean("isLoggedIn", false);
				pref.putString("username", "!error!");
				pref.putString("passhash", "!error!");
				pref.putLong("lastgamesync", 0);
				pref.putLong("lastmsgsync", 0);
				pref.commit();

				EditText txt = (EditText) act.findViewById(R.id.username);
				txt.setText("");

				txt = (EditText) act.findViewById(R.id.password);
				txt.setText("");
				break;
			case ProgressMsg.MSG:
				if (exitActivity && !isTablet)
					act.finish();
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
				Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

			switch (msg.what) {
			case NetworkClient.LOGIN:
				progress.setText("Syncing Data");

				EditText txt = (EditText) act.findViewById(R.id.username);
				final String username = txt.getText().toString().trim();

				txt = (EditText) act.findViewById(R.id.password);
				final String password = txt.getText().toString();

				final Editor pref = PreferenceManager.getDefaultSharedPreferences(act).edit();
				pref.putBoolean("isLoggedIn", true);
				pref.putString("username", username);
				pref.putString("passhash", password);
				pref.commit();

				SocketClient.getInstance().setIsLoggedIn(true);

				final SyncClient sync = new SyncClient(act, handle);
				sync.setSyncType(SyncClient.FULL_SYNC);
				(new Thread(sync)).start();
				break;
			case SyncClient.MSG:
				// start background notifier
				act.startService(new Intent(act, GenesisNotifier.class));

				final GameDataDB db = new GameDataDB(act);
				db.recalcYourTurn();
				db.close();

				sendResult(Activity.RESULT_OK);
				progress.dismiss();
				break;
			}
		}
	};

	public void setCallBack(final int value)
	{
		callbackId = value;
	}

	private void sendResult(final int result)
	{
		if (isTablet) {
			final MainMenuFrag frag = (MainMenuFrag) fragMan.findFragmentById(R.id.panel01);
			frag.startFragment(callbackId);
		} else {
			exitActivity = true;
			act.setResult(result);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		initBaseContentFrag();

		final View view = inflater.inflate(R.layout.fragment_login, container, false);

		// create network client instance
		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act, handle);

		// setup click listeners
		ImageView image = (ImageView) view.findViewById(R.id.login);
		image.setOnClickListener(this);
		image = (ImageView) view.findViewById(R.id.register);
		image.setOnClickListener(this);

		// Always show the currently logged in user
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		if (pref.getBoolean("isLoggedIn", false)) {
			EditText txt = (EditText) view.findViewById(R.id.username);
			txt.setText(pref.getString("username", ""));

			txt = (EditText) view.findViewById(R.id.password);
			txt.setText("");
		}
		return view;
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
			progress.setText("Requesting Login");

			EditText txt = (EditText) act.findViewById(R.id.username);
			final String username = txt.getText().toString().trim();

			txt = (EditText) act.findViewById(R.id.password);
			final String password = txt.getText().toString();

			net.login_user(username, password);
			(new Thread(net)).start();
			break;
		case R.id.register:
			if (isTablet) {
				final FragmentIntent fintent = new FragmentIntent();
				fintent.setFrag(R.id.panel02, new RegisterFrag(), RegisterFrag.TAG);
				fintent.loadFrag(fragMan);
			} else {
				startActivityForResult(new Intent(act, Register.class), 1);
			}
			break;
		case R.id.menu:
			openMenu(v);
			break;
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = TAG;

		act.getMenuInflater().inflate(R.menu.options_login, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (act.lastContextMenu == TAG)
			return onOptionsItemSelected(item);
		else
			return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.logout) {
			(new LogoutConfirm(act, handle)).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onActivityResult(final int reques, final int result, final Intent data)
	{
		String username = "";
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);

		if (pref.getBoolean("isLoggedIn", false))
			username = pref.getString("username", "");

		EditText txt = (EditText) act.findViewById(R.id.username);
		txt.setText(username);

		txt = (EditText) act.findViewById(R.id.password);
		txt.setText("");
	}
}