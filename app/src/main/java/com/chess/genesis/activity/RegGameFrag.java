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
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;

public class RegGameFrag extends GameFrag
{
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		// restore settings
		final Bundle settings;
		if (savedInstanceState != null)
			setArguments(settings = savedInstanceState);
		else
			settings = getArguments();

		type = settings.getInt("type");

		// set playingBlack
		final boolean playingBlack = type != Enums.LOCAL_GAME?
			settings.getString("username").equals(settings.getString("black")) :
			Integer.parseInt(settings.getString("opponent")) == Enums.CPU_WHITE_OPPONENT;

		// set view as black
		viewAsBlack = Pref.getBool(act, R.array.pf_viewAsBlack) && playingBlack;

		// create game stat instance
		gamestate = new RegGameState(this);

		// finalize initialization
		return super.onCreateView(inflater, container, settings);
	}
}
