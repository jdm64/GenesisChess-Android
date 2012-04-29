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

package com.chess.genesis.data;

import android.content.*;
import android.preference.*;
import com.chess.genesis.engine.*;

class GameInfo
{
	private final Context context;
	private final String history;
	private final String white;
	private final int status;
	private final int draw;

	public GameInfo(final Context _context, final int Status, final String History, final String White, final int Draw)
	{
		history = History;
		white = White;
		status = Status;
		context = _context;
		draw = Draw;
	}

	public int getPly()
	{
		if (history == null || history.length() < 3)
			return 0;
		return history.trim().split(" +").length;
	}

	public int getYourTurn()
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		if (status != Enums.ACTIVE)
			return Enums.YOUR_TURN;

		final int color = white.equals(pref.getString("username", "!error!"))? Piece.WHITE : Piece.BLACK;

		if (draw != 0)
			return (color * draw > 0)? Enums.THEIR_TURN : Enums.YOUR_TURN;
		else if (status != Enums.ACTIVE)
			return Enums.YOUR_TURN;

		final int stm = (getPly() % 2 == 0)? Piece.WHITE : Piece.BLACK;
		return (stm == color)? Enums.YOUR_TURN : Enums.THEIR_TURN;
	}
}
