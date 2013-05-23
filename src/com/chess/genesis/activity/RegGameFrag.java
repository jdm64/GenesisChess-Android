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
import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;

public class RegGameFrag extends GameFrag
{
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		// must be called before new GameState
		initBaseContentFrag(container);

		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getArguments();
		type = settings.getInt("type");

		// set playingBlack
		final boolean playingBlack = type != Enums.LOCAL_GAME?
			settings.getString("username").equals(settings.getString("black")) :
			Integer.parseInt(settings.getString("opponent")) == Enums.CPU_WHITE_OPPONENT;

		// set view as black
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
		viewAsBlack = pref.getBoolean(PrefKey.VIEW_AS_BLACK, true) && playingBlack;

		// create game stat instance
		gamestate = new RegGameState(act, this, settings);

		// finalize initialization
		return super.onCreateView(inflater, container, settings);
	}

	@Override
	public void reset()
	{
		super.reset();

		for (int i = 0; i < 64; i++) {
			final BoardButton square = (BoardButton) act.findViewById(i);
			square.setPiece(Piece.EMPTY);
		}
		for (int i = 0; i < 32; i++) {
			final int loc = BaseBoard.EE64(RegBoard.InitRegPiece[i]);
			final BoardButton square = (BoardButton) act.findViewById(loc);
			square.setPiece(Move.InitPieceType[i]);
		}
	}
}
