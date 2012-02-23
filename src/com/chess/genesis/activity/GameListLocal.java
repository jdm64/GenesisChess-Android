package com.chess.genesis;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class GameListLocal extends BasePhoneActivity implements OnClickListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new GameListLocalFrag(), GameListLocalFrag.TAG, R.layout.activity_gamelist);

		// set click listeners
		final ImageView button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.topbar_plus)
			(new NewLocalGameDialog(v.getContext(), ((GameListLocalFrag) mainFrag).handle)).show();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (resultCode == RESULT_CANCELED || data == null)
			return;

		((GameListLocalFrag) mainFrag).recieveGame(data);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_gamelist_local, menu);
		return true;
	}
}
