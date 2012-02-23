package com.chess.genesis;

import android.os.Bundle;

public class UserStats extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new UserStatsFrag(), UserStatsFrag.TAG, R.layout.activity_basephone);
	}
}
