package com.chess.genesis;

import android.os.Bundle;
import android.view.Menu;

public class MsgBox extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, mainFrag = new MsgBoxFrag(), MsgBoxFrag.TAG, R.layout.activity_basephone);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_msgbox, menu);
		return true;
	}
}
