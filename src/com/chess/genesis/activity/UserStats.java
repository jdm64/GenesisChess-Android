package com.chess.genesis;

import android.os.Bundle;
import android.view.Menu;

public class UserStats extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new UserStatsFrag(), UserStatsFrag.TAG, R.layout.activity_basephone);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_userstats, menu);
		return true;
	}
}
