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

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import org.json.*;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener, OnLongClickListener, CallBackPreference.CallBack
{
	private Context context;
	private NetworkClient net;
	private ProgressMsg progress;
	private SharedPreferences pref;

	private final Handler handle = new Handler()
	{
		@Override
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;
		try {
			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			switch (msg.what) {
			case NetworkClient.GET_OPTION:
				// only emailnote supported
				final CheckBoxPreference email = (CheckBoxPreference) findPreference("emailNoteEnabled");
				email.setChecked(json.getBoolean("value"));

				progress.remove();
				break;
			case NetworkClient.SET_OPTION:
				progress.remove();
				break;
			case SyncClient.MSG:
				final GameDataDB db = new GameDataDB(context);
				db.recalcYourTurn();
				db.close();
				progress.remove();
				break;
			}
		} catch (final JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;

		// set layout mode
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean isTablet = pref.getBoolean("tabletMode", false);

		if (isTablet)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_settings);
		addPreferencesFromResource(R.xml.settings);

		net = new NetworkClient(this, handle);
		progress = new ProgressMsg(this);

		final View button = findViewById(R.id.topbar_genesis);
		button.setOnLongClickListener(this);

		final boolean isLoggedin = pref.getBoolean("isLoggedIn", false);

		final Preference bench = findPreference("benchmark");
		bench.setOnPreferenceClickListener(this);

		final IntListPreference poll = (IntListPreference) findPreference("notifierPolling");
		poll.setOnPreferenceChangeListener(this);

		CheckBoxPreference check = (CheckBoxPreference) findPreference("emailNoteEnabled");
		check.setOnPreferenceChangeListener(this);
		check.setEnabled(isLoggedin);

		check = (CheckBoxPreference) findPreference("noteEnabled");
		check.setOnPreferenceChangeListener(this);
		check.setEnabled(isLoggedin);

		CallBackPreference callbackPref = (CallBackPreference) findPreference("deleteLocalTable");
		callbackPref.setCallBack(this);
		callbackPref = (CallBackPreference) findPreference("resyncOnlineTable");
		callbackPref.setCallBack(this);
		callbackPref.setEnabled(isLoggedin);
		callbackPref = (CallBackPreference) findPreference("resyncArchiveTable");
		callbackPref.setCallBack(this);
		callbackPref.setEnabled(isLoggedin);
		callbackPref = (CallBackPreference) findPreference("resyncMsgTable");
		callbackPref.setCallBack(this);
		callbackPref.setEnabled(isLoggedin);

		// Set email note value from server
		if (pref.getBoolean("isLoggedIn", false)) {
			progress.setText("Retrieving Settings");
			net.get_option("emailnote");
			new Thread(net).start();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (pref.getBoolean("isLoggedIn", false))
			NetActive.inc();
		AdsHandler.run(this);
	}

	@Override
	public void onPause()
	{
		if (pref.getBoolean("isLoggedIn", false))
			NetActive.dec();

		super.onPause();
	}

	@Override
	public void runCallBack(final CallBackPreference preference, final boolean result)
	{
		if (!result)
			return;

		final String key = preference.getKey();
		final GameDataDB db = new GameDataDB(this);
		final SyncClient sync = new SyncClient(this, handle);

		if (key.equals("deleteLocalTable")) {
			db.deleteAllLocalGames();
		} else if (key.equals("resyncOnlineTable")) {
			progress.setText("ReSyncing Active Games");

			sync.setSyncType(SyncClient.ACTIVE_SYNC);
			new Thread(sync).start();
		} else if (key.equals("resyncArchiveTable")) {
			progress.setText("ReSyncing Archive Games");

			sync.setSyncType(SyncClient.ARCHIVE_SYNC);
			new Thread(sync).start();
		} else if (key.equals("resyncMsgTable")) {
			progress.setText("ReSyncing Messages");

			sync.setSyncType(SyncClient.MSG_SYNC);
			new Thread(sync).start();
		}
		db.close();
	}

	@Override
	public boolean onPreferenceChange(final Preference preference, final Object newValue)
	{
		final String key = preference.getKey();

		if (key.equals("emailNoteEnabled")) {
			progress.setText("Setting Option");

			net.set_option("emailnote", (Boolean) newValue);
			new Thread(net).start();
		} else if (key.equals("noteEnabled") || key.equals("notifierPolling")) {
			startService(new Intent(this, GenesisNotifier.class));
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(final Preference preference)
	{
		final String key = preference.getKey();

		if (key.equals("benchmark"))
			new BenchmarkDialog(this).show();
		return true;
	}

	@Override
	public boolean onLongClick(final View v)
	{
		if (v.getId() == R.id.topbar_genesis) {
			finish();
			return true;
		}
		return false;
	}
}
