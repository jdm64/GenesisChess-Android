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

package com.chess.genesis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class GenGameFrag extends GameFrag implements OnClickListener
{
	public final static String TAG = "GAME";

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		// must be called before new GameState
		initBaseContentFrag();

		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getArguments();
		type = settings.getInt("type");

		// set view as black
		viewAsBlack = false;

		// create game state instance
		gamestate = new GenGameState(act, this, settings);

		// finalize initialization
		return super.onCreateView(inflater, container, settings);
	}

	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) act.findViewById(i);
			square.resetSquare();
		}
	}
}
