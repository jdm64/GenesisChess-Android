/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.activity;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;

public class GenGameFrag extends GameFrag
{
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		// must be called before new GameState
		initBaseContentFrag(container);

		// restore settings
		if (savedInstanceState != null)
			setArguments(savedInstanceState);

		// set view as black
		viewAsBlack = false;

		// create game state instance
		gamestate = new GenGameState(this);

		// finalize initialization
		return super.onCreateView(inflater, container, getArguments());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		if (pref.getBoolean(PrefKey.SHOW_GENESIS_RULES, true))
			new GenesisRulesDialog(act).show();
	}

	@Override
	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) act.findViewById(i);
			square.reset();
		}
	}
}
