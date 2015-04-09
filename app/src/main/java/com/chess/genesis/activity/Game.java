/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.activity;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

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

		final int gametype = Integer.parseInt(settings.getString("gametype"));
		final BaseContentFrag frag = (gametype == Enums.GENESIS_CHESS)?
			new GenGameFrag() : new RegGameFrag();

		// initialize layout
		super.onCreate(savedInstanceState, frag, layoutId);

		// set click listeners
		if (type != Enums.LOCAL_GAME) {
			final View button = findViewById(R.id.chat);
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState)
	{
		savedInstanceState.putAll(settings);
		super.onSaveInstanceState(savedInstanceState);
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

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.chat) {
			final Intent intent = new Intent(this, MsgBox.class);
			intent.putExtra("gameid", mainFrag.getArguments().getString("gameid"));
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
			getMenuInflater().inflate(R.menu.options_game_online, menu);
			break;
		case Enums.ARCHIVE_GAME:
			getMenuInflater().inflate(R.menu.options_game_archive, menu);
			break;
		}
		return true;
	}
}
