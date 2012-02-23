package com.chess.genesis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.EditText;

public class Login extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new LoginFrag(), LoginFrag.TAG, R.layout.activity_basephone);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_login, menu);
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
