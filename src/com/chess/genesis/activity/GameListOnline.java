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

import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import com.chess.genesis.*;
import com.chess.genesis.dialog.*;

public class GameListOnline extends BasePhoneActivity implements OnClickListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new GameListOnlineFrag(), R.layout.activity_gamelist);

		// set click listeners
		final View button = findViewById(R.id.topbar_plus);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.topbar_plus)
			new NewOnlineGameDialog(v.getContext(), ((GameListOnlineFrag) mainFrag).handle).show();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_gamelist_online, menu);
		return true;
	}
}
