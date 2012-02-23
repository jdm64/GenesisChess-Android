package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class StartActivity extends Activity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// run upgrade
		UpgradeHandler.run(this);

		// set layout mode
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean isTablet = pref.getBoolean("tabletMode", false);

		if (isTablet)
			startActivity(new Intent(this, MainMenuTablet.class));
		else
			startActivity(new Intent(this, MainMenuPhone.class));
	}

	@Override
	public void onResume()
	{
		super.onResume();
		finish();
	}
}
