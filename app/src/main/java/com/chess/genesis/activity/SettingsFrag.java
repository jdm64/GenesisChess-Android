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
import android.view.*;
import android.view.View.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.R;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import androidx.preference.*;
import androidx.preference.Preference.*;

public class SettingsFrag extends PreferenceFragmentCompat implements
	OnPreferenceChangeListener, OnPreferenceClickListener, OnLongClickListener,
	CallBackPreference.CallBack, Handler.Callback, SharedPreferences.OnSharedPreferenceChangeListener
{
	private final Handler handle = new Handler(this);
	private Activity context;
	private NetworkClient net;
	private ProgressMsg progress;

	@Override
	public boolean handleMessage(final Message msg)
	{
		final JSONObject json = (JSONObject) msg.obj;
	try {
		if (json.getString("result").equals("error")) {
			progress.dismiss();
			Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
			return true;
		}
		switch (msg.what) {
		case NetworkClient.GET_OPTION:
			// only emailnote supported
			final CheckBoxPreference email = (CheckBoxPreference) findPreference(Pref.key(context, R.array.pf_emailNoteEnabled));
			email.setChecked(json.getBoolean("value"));

			progress.dismiss();
			break;
		case NetworkClient.SET_OPTION:
			progress.dismiss();
			break;
		case SyncClient.MSG:
			final GameDataDB db = new GameDataDB(context);
			db.recalcYourTurn();
			db.close();
			progress.dismiss();
			break;
		}
		return true;
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		context = getActivity();

		final Pref pref = new Pref(context);
		pref.setChangeListener(this);

		addPreferencesFromResource(R.xml.settings);

		net = new NetworkClient(context, handle);
		progress = new ProgressMsg(context);

		final boolean isLoggedin = pref.getBool(R.array.pf_isLoggedIn);

		Preference prefView = findPreference("benchmark");
		prefView.setOnPreferenceClickListener(this);

		prefView = findPreference(pref.key(R.array.pf_notifierPolling));
		prefView.setOnPreferenceChangeListener(this);

		prefView = findPreference(pref.key(R.array.pf_emailNoteEnabled));
		prefView.setOnPreferenceChangeListener(this);
		prefView.setEnabled(isLoggedin);

		prefView = findPreference(pref.key(R.array.pf_noteEnabled));
		prefView.setOnPreferenceChangeListener(this);
		prefView.setEnabled(isLoggedin);

		String[] keys = new String[]{"bcInnerDark", "bcOuterDark", "bcInnerLight", "bcOuterLight",
			"bcInnerSelect", "bcInnerCheck", "bcInnerLast"};
		for (String key : keys) {
			ColorPickerPreference picker = (ColorPickerPreference) findPreference(key);
			picker.setFragMan(getFragmentManager());
		}

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
		callbackPref = (CallBackPreference) findPreference("bcReset");
		callbackPref.setCallBack(this);

		// Set email note value from server
		if (pref.getBool(R.array.pf_isLoggedIn)) {
			progress.setText("Retrieving Settings");
			net.get_option("emailnote");
			new Thread(net).start();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (Pref.getBool(context, R.array.pf_isLoggedIn))
			NetActive.inc();
	}

	@Override
	public void onPause()
	{
		if (Pref.getBool(context, R.array.pf_isLoggedIn))
			NetActive.dec(context);

		super.onPause();
	}

	@Override
	public void runCallBack(final CallBackPreference preference)
	{
		final String key = preference.getKey();
		final GameDataDB db = new GameDataDB(context);
		final SyncClient sync = new SyncClient(context, handle);

		switch (key) {
		case "deleteLocalTable":
			db.deleteAllLocalGames();
			break;
		case "resyncOnlineTable":
			progress.setText("ReSyncing Active Games");

			sync.setSyncType(SyncClient.ACTIVE_SYNC);
			new Thread(sync).start();
			break;
		case "resyncArchiveTable":
			progress.setText("ReSyncing Archive Games");

			sync.setSyncType(SyncClient.ARCHIVE_SYNC);
			new Thread(sync).start();
			break;
		case "resyncMsgTable":
			progress.setText("ReSyncing Messages");

			sync.setSyncType(SyncClient.MSG_SYNC);
			new Thread(sync).start();
			break;
		case "bcReset":
			resetBoardColors();
			break;
		}
		db.close();
	}

	@Override
	public boolean onPreferenceChange(final Preference preference, final Object newValue)
	{
		final String key = preference.getKey();
		final Pref pref = new Pref(context);

		if (key.equals(pref.key(R.array.pf_emailNoteEnabled))) {
			progress.setText("Setting Option");

			net.set_option("emailnote", newValue);
			new Thread(net).start();
		} else if (key.equals(pref.key(R.array.pf_noteEnabled)) || key.equals(pref.key(R.array.pf_notifierPolling))) {
			context.startService(new Intent(context, GenesisNotifier.class));
		}
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		SocketClient.initHost(context);
		SocketClient.initDebug(context);
	}

	@Override
	public boolean onPreferenceClick(final Preference preference)
	{
		final String key = preference.getKey();

		if (key.equals("benchmark"))
			BenchmarkDialog.create().show(getFragmentManager(), "");
		return true;
	}

	@Override
	public boolean onLongClick(final View v)
	{
		if (v.getId() == R.id.topbar_genesis) {
			context.finish();
			return true;
		}
		return false;
	}

	private void resetBoardColors()
	{
		PieceImgPainter.resetColors(context);

		final int[] keys = new int[]{R.array.pf_bcInnerCheck, R.array.pf_bcInnerDark,
			R.array.pf_bcInnerLast, R.array.pf_bcInnerLight, R.array.pf_bcInnerSelect,
			R.array.pf_bcOuterDark, R.array.pf_bcOuterLight };
		final Pref pref = new Pref(context);

		for (final int key : keys) {
			final ColorPickerPreference colorPref = (ColorPickerPreference) findPreference(pref.key(key));
			colorPref.update();
		}
	}
}
