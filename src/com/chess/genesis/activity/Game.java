package com.chess.genesis;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class Game extends BasePhoneActivity implements OnClickListener
{
	private Bundle settings;
	private int type;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getIntent().getExtras();
		type = settings.getInt("type");

		// set content view layout id
		final int layoutId = (type != Enums.LOCAL_GAME)?
			R.layout.activity_game_online : R.layout.activity_game_local;

		final int gametype = Integer.valueOf(settings.getString("gametype"));
		final BaseContentFrag frag = (gametype == Enums.GENESIS_CHESS)?
			new GenGameFrag() : new RegGameFrag();

		// initialize layout
		super.onCreate(savedInstanceState, frag, GameFrag.TAG, layoutId);

		// set click listeners
		if (type != Enums.LOCAL_GAME) {
			final View button = findViewById(R.id.chat);
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (type != Enums.LOCAL_GAME) {
			final GameDataDB db = new GameDataDB(this);
			final int count = db.getUnreadMsgCount(settings.getString("gameid"));
			final int img = (count > 0)? R.drawable.newmsg : R.drawable.chat;

			db.close();

			final ImageView v = (ImageView) findViewById(R.id.chat);
			v.setImageResource(img);
		}
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.chat) {
			final Intent intent = new Intent(this, MsgBox.class);
			intent.putExtra("gameid", ((GameFrag) mainFrag).settings.getString("gameid"));
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		switch (((GameFrag) mainFrag).type) {
		case Enums.LOCAL_GAME:
			getMenuInflater().inflate(R.menu.options_game_local, menu);
			break;
		case Enums.ONLINE_GAME:
			if (Integer.valueOf(((GameFrag) mainFrag).settings.getString("ply")) > 58)
				getMenuInflater().inflate(R.menu.options_game_online_draw, menu);
			else
				getMenuInflater().inflate(R.menu.options_game_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			getMenuInflater().inflate(R.menu.options_game_archive, menu);
			break;
		}
		return true;
	}
}
