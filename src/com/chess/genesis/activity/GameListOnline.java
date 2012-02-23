package com.chess.genesis;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class GameListOnline extends BasePhoneActivity implements OnClickListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new GameListOnlineFrag(), GameListOnlineFrag.TAG, R.layout.activity_gamelist);

		// set click listeners
		final ImageView button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.topbar_plus)
			(new NewOnlineGameDialog(v.getContext(), ((GameListOnlineFrag) mainFrag).handle)).show();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_gamelist_online, menu);
		return true;
	}
}
