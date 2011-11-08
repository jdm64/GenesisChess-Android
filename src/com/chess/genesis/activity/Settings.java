package com.chess.genesis;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener, OnLongClickListener, OnTouchListener
{
	private static Settings self;

	private static NetworkClient net;
	private static ProgressMsg progress;
	private static SharedPreferences pref;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;
		try {
			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
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

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Set layouts
		setContentView(R.layout.activity_settings);
		addPreferencesFromResource(R.xml.settings);

		net = new NetworkClient(this, handle);
		progress = new ProgressMsg(this);
		pref = PreferenceManager.getDefaultSharedPreferences(this);

		final ImageView button = (ImageView) findViewById(R.id.topbar);
		button.setOnTouchListener(this);
		button.setOnLongClickListener(this);

		CheckBoxPreference check = (CheckBoxPreference) findPreference("emailNoteEnabled");
		check.setOnPreferenceChangeListener(this);
		check.setEnabled(pref.getBoolean("isLoggedIn", false));

		check = (CheckBoxPreference) findPreference("noteEnabled");
		check.setOnPreferenceChangeListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (pref.getBoolean("isLoggedIn", false)) {
			NetActive.inc();

			progress.setText("Retrieving Settings");

			net.get_option("emailnote");
			(new Thread(net)).start();
		}
	}

	@Override
	public void onPause()
	{
		if (pref.getBoolean("isLoggedIn", false))
			NetActive.dec();

		super.onPause();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		final String key = preference.getKey();

		if (key.equals("emailNoteEnabled")) {
			progress.setText("Setting Option");

			net.set_option("emailnote", ((Boolean) newValue).booleanValue());
			(new Thread(net)).start();
		} else if (key.equals("noteEnabled")) {
			startService(new Intent(this, GenesisNotifier.class));
		}
		return true;
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
		}
		return false;
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
}
