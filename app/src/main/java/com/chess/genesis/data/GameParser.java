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

package com.chess.genesis.data;

import android.os.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;
import org.json.*;

public class GameParser
{
	static class GamePosition
	{
		public String history;
		public String zfen;
	}

	private GameParser()
	{
	}

	private static GamePosition parsePosition(final String _history, final int gametype)
	{
		final GamePosition pos = new GamePosition();
		final Board board = (gametype == Enums.GENESIS_CHESS)? new GenBoard() : new RegBoard();

		if (_history.length() < 3) {
			pos.history = " ";
			pos.zfen = board.printZfen();
			return pos;
		}
		final String[] movehistory = _history.trim().split(" +");
		final ObjectArray<Move> history = new ObjectArray<>(board.moveGenerator());

		for (final String element : movehistory) {
			final Move move = board.newMove();
			move.parse(element);

			if (board.validMove(move) != Move.VALID_MOVE)
				break;
			history.push(move);
			board.make(move);
		}
		pos.history = history.toString();
		pos.zfen = board.printZfen();
		return pos;
	}

	public static Bundle parse(final JSONObject data)
	{
		final Bundle game = new Bundle();

	try {
		game.putInt("gametype", Enums.GameType(data.optString("gametype", "genesis")));
	} catch (final RuntimeException e) {
		game.putInt("gametype", Enums.GENESIS_CHESS);
	}
	try {
		game.putInt("opponent", Enums.OpponentType(data.optString("opponent", "human")));
	} catch (final RuntimeException e) {
		game.putInt("opponent", Enums.HUMAN_OPPONENT);
	}

		game.putString("name", data.optString("name", "untitled"));
		game.putLong("ctime", data.optLong("ctime", System.currentTimeMillis()));
		game.putLong("stime", data.optLong("stime", System.currentTimeMillis()));

		final GamePosition pos = parsePosition(data.optString("history", " "), game.getInt("gametype"));
		game.putString("history", pos.history);
		game.putString("zfen", pos.zfen);

		return game;
	}

	public static JSONObject parse(final Bundle data) throws JSONException
	{
		final JSONObject game = export(data);

		game.put("zfen", data.getString("zfen"));
		game.put("ctime", Long.parseLong(data.getString("ctime")));
		game.put("stime", Long.parseLong(data.getString("stime")));

		return game;
	}

	public static JSONObject export(final Bundle data) throws JSONException
	{
		final JSONObject game = new JSONObject();

		// check if game is a local game
		final String name;
		if (data.getString("name") == null) {
			name = data.getString("white") + " Vs. " + data.getString("black");
			game.put("eventtype", Enums.EventType(Integer.parseInt(data.getString("gametype"))));
		} else {
			name = data.getString("name");
			game.put("opponent", Enums.OpponentType(Integer.parseInt(data.getString("opponent"))));
		}

		game.put("name", name);
		game.put("gametype", Enums.GameType(Integer.parseInt(data.getString("gametype"))));
		game.put("history", data.getString("history"));

		return game;
	}
}
