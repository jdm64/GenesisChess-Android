package com.chess.genesis;

import android.os.Bundle;

public class Register extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new RegisterFrag(), RegisterFrag.TAG, R.layout.activity_basephone);
	}
}
