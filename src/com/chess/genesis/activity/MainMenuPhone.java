package com.chess.genesis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;

public class MainMenuPhone extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new MainMenuFrag(), MainMenuFrag.TAG, R.layout.activity_basephone);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_mainmenu, menu);
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (resultCode == RESULT_CANCELED)
			return;

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (requestCode == Enums.ONLINE_LIST) {
			startActivity(new Intent(this, GameListOnline.class));
		} else if (requestCode == Enums.USER_STATS) {
			final Intent intent = new Intent(this, UserStats.class);
			intent.putExtra("username", pref.getString("username", "!error!"));
			startActivity(intent);
		}
	}
}
