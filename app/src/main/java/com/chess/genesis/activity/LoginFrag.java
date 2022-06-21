/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.activity;

import android.app.*;
import android.content.*;
import android.os.*;
import android.os.Handler.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.activity.BroadcastWrapper.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;

public class LoginFrag extends AbstractActivityFrag implements Callback, Receiver, OnClickListener
{
	private final Handler handle = new Handler(this);
	private NetworkClient net;
	private ProgressMsg progress;
	private int callbackId = Enums.NO_ACTIVITY;
	private boolean exitActivity = false;
	private final BroadcastWrapper bw = new BroadcastWrapper(this);

	public LoginFrag()
	{
		setArguments(new Bundle());
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what) {
		case NetworkClient.LOGIN:
		case SyncClient.MSG:
			handleNetwork(msg);
			break;
		case LogoutConfirm.MSG:
			new PrefEdit(act)
				.putBool(R.array.pf_isLoggedIn)
				.putString(R.array.pf_username)
				.putString(R.array.pf_passhash)
				.putLong(R.array.pf_lastgamesync)
				.putLong(R.array.pf_lastmsgsync)
				.commit();

			EditText txt = act.findViewById(R.id.username);
			txt.setText("");

			txt = act.findViewById(R.id.password);
			txt.setText("");
			break;
		case ProgressMsg.MSG:
			if (exitActivity)
				act.finish();
			break;
		}
		return true;
	}

	private void handleNetwork(final Message msg)
	{
		final JSONObject json = (JSONObject) msg.obj;

	try {
		if (json.getString("result").equals("error")) {
			exitActivity = false;
			progress.dismiss();
			Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
			return;
		}
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}

		switch (msg.what) {
		case NetworkClient.LOGIN:
			progress.setText("Syncing Data");

			final String username = getArguments().getString("username");
			final String password = getArguments().getString("password");

			new PrefEdit(act)
				.putBool(R.array.pf_isLoggedIn, true)
				.putString(R.array.pf_username, username)
				.putString(R.array.pf_passhash, password)
				.commit();

			SocketClient.getInstance(getActivity()).setIsLoggedIn(true);

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

	public void setCallBack(final int value)
	{
		callbackId = value;
	}

	private void sendResult(final int result)
	{
		exitActivity = true;
		act.setResult(result);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		bw.setFilter(new IntentFilter("register"));
		bw.register();

		// create network client instance
		net = new NetworkClient(getActivity(), handle);
		progress = new ProgressMsg(getActivity(), handle);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_login, container, false);

		// setup click listeners
		View image = view.findViewById(R.id.login);
		image.setOnClickListener(this);
		image = view.findViewById(R.id.register);
		image.setOnClickListener(this);

		// Always show the currently logged in user
		final Pref pref = new Pref(act);
		if (pref.getBool(R.array.pf_isLoggedIn)) {
			final EditText txt = view.findViewById(R.id.username);
			txt.setText(pref.getString(R.array.pf_username));
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
		NetActive.dec(getActivity());
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		bw.unregister();
		super.onDestroy();
	}

	@Override
	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.login:
			EditText txt = act.findViewById(R.id.username);
			final String username = txt.getText().toString().trim();

			txt = act.findViewById(R.id.password);
			final String password = txt.getText().toString();

			getArguments().putString("username", username);
			getArguments().putString("password", password);

			doLogin(username, password);
			break;
		case R.id.register:
			startActivity(new Intent(act, Register.class));
			break;
		}
	}

	private void doLogin(String username, String password)
	{
		progress.setText("Requesting Login");
		net.login_user(username, password);
		new Thread(net).start();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_login, menu);

		if (!new Pref(act).getBool(R.array.pf_isLoggedIn)) {
			menu.removeItem(R.id.logout);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.logout) {
			LogoutConfirm.create(handle).show(getFragmentManager(), "");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onReceive(Intent intent)
	{
		final Bundle data = intent.getExtras();
		getArguments().putAll(data);
		doLogin(data.getString("username"), data.getString("password"));
	}
}
