/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.activity;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import org.json.*;

public class LoginFrag extends BaseContentFrag
{
	public final static String TAG = "LOGIN";

	private NetworkClient net;
	private ProgressMsg progress;
	private int callbackId = Enums.NO_ACTIVITY;
	private boolean exitActivity = false;

	public final Handler handle = new Handler()
	{
		@Override
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
		} catch (final JSONException e) {
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
				new Thread(sync).start();
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
		View image = view.findViewById(R.id.login);
		image.setOnClickListener(this);
		image = view.findViewById(R.id.register);
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

	@Override
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
			new Thread(net).start();
			break;
		case R.id.register:
			if (isTablet) {
				final FragmentIntent fintent = new FragmentIntent();
				fintent.setActivity(act);
				fintent.setFrag(R.id.panel02, new RegisterFrag(), RegisterFrag.TAG);
				fintent.loadFrag(fragMan);
			} else {
				startActivityForResult(new Intent(act, Register.class), Enums.REGISTER);
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
		if (act.lastContextMenu.equals(TAG))
			return onOptionsItemSelected(item);
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.logout) {
			new LogoutConfirm(act, handle).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
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
