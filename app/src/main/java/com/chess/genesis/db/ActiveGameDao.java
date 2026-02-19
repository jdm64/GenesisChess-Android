/* GenesisChess, an Android chess application
 * Copyright 2022, Justin Madru (justin.jdm64@gmail.com)
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
package com.chess.genesis.db;

import java.util.*;
import android.content.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import com.chess.genesis.data.Enums.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.msgs.*;
import com.chess.genesis.util.*;
import androidx.paging.*;
import androidx.room.*;

@Dao
public interface ActiveGameDao
{
	static ActiveGameDao get(Context context)
	{
		return GameDatabase.getInstance(context).activeGameDao();
	}

	default ActiveGameEntity newLocalGame(NewGameState data)
	{
		var type = data.getType().getValue().norm();
		var oppCat = data.getOpp().getValue();
		var color = data.getColor().getValue().norm();
		var opp = oppCat.toType(color);
		var clockType = data.getClockType().getValue();
		var baseTime = data.getBaseTime().getValue();
		var incTime = data.getIncTime().getValue();

		if (clockType == ClockType.NO_CLOCK) {
			baseTime = 0;
			incTime = 0;
		}

		var game = new ActiveGameEntity();
		game.gameid = UUID.randomUUID().toString();
		game.name = "Untitled";

		game.eventType = EventType.LOCAL.id;
		game.gametype = type.id;
		game.opponent = opp.id;
		game.status = GameStatus.ACTIVE.id;

		game.clockType = clockType.id;
		game.baseTime = baseTime;
		game.incTime = incTime;
		game.whiteTime = baseTime * 1000;
		game.blackTime = baseTime * 1000;

		game.zfen = (type == GameType.GENESIS ? new GenBoard() : new RegBoard()).printZFen();
		game.history = "";
		game.ctime = System.currentTimeMillis();
		game.stime = System.currentTimeMillis();

		insert(game);

		return game;
	}

	default ActiveGameEntity importInviteGame(String gameId, int gameType, int color)
	{
		var game = new ActiveGameEntity();
		game.gameid = gameId;
		game.name = "Invite " + Enums.from(GameType.class, gameType) + "; Play " + (color == Piece.WHITE ? "white" : "black");
		game.gametype = gameType;
		game.opponent = color == Piece.WHITE ? OpponentType.REMOTE_BLACK.id : OpponentType.REMOTE_WHITE.id;
		game.zfen = (gameType == GameType.GENESIS.id ? new GenBoard() : new RegBoard()).printZFen();
		game.history = "";
		game.ctime = System.currentTimeMillis();
		game.stime = System.currentTimeMillis();

		insert(game);

		return game;
	}

	default ActiveGameEntity importInviteGame(ActiveGameDataMsg msg, Context ctx)
	{
		var game = new ActiveGameEntity();

		String name;
		var opp = msg.getOpponent(ctx);
		if (opp.isEmpty()) {
			name = "Invite Game";
		} else {
			name = "Game with " + opp;
		}

		game.gameid = msg.game_id;
		game.name = name;
		game.white = msg.white;
		game.black = msg.black;

		game.eventType = msg.event_type;
		game.gametype = msg.game_type;
		game.opponent = msg.getOpponentType(ctx);
		game.status = msg.status;

		game.clockType = msg.clock_type;
		game.baseTime = msg.base_time;
		game.incTime = msg.inc_time;
		game.whiteTime = msg.white_time;
		game.blackTime = msg.black_time;
		game.ctime = msg.create_time;
		game.stime = msg.save_time;

		game.history = msg.movesString();
		game.zfen = msg.zfen;

		insert(game);

		return game;
	}

	default ActiveGameEntity updateActiveGame(ActiveGameDataMsg msg, Context ctx)
	{
		var game = getGame(msg.game_id);
		if (game == null) {
			Util.logErr("game not found: " + msg.game_id, this);
			return null;
		}

		var updateRequired = !msg.movesString().equals(game.history);

		game.white = msg.white;
		game.black = msg.black;

		game.stime = msg.save_time;
		game.status = msg.status;

		game.whiteTime = msg.white_time;
		game.blackTime = msg.black_time;

		game.zfen = msg.zfen;
		game.history = msg.movesString();

		update(game);

		return updateRequired ? game : null;
	}

	default boolean saveMove(String gameId, int index, String move)
	{
		var game = getGame(gameId);
		if (game == null) {
			return false;
		}

		var board = game.gametype == GameType.GENESIS.id ? new GenBoard() : new RegBoard();
		if (!board.parseZFen(game.zfen)) {
			return false;
		} else if (board.getPly() + 1 != index) {
			return false;
		}

		var res = board.parseMove(move);
		if (res.second != Board.VALID_MOVE) {
			return false;
		}

		game.history += " " + res.first;
		game.stime = System.currentTimeMillis();

		update(game);
		return true;
	}

	default boolean saveMove(LastMoveMsg msg)
	{
		var game = getGame(msg.id);
		if (game != null && saveMove(game, msg)) {
			update(game);
			return true;
		}

		return false;
	}

	static boolean saveMove(ActiveGameEntity game, LastMoveMsg msg)
	{
		var board = game.gametype == GameType.GENESIS.id ? new GenBoard() : new RegBoard();
		if (!board.parseZFen(game.zfen)) {
			return false;
		} else if (board.getPly() != msg.index) {
			return false;
		}

		var res = board.parseMove(msg.move);
		if (res.second != Board.VALID_MOVE) {
			return false;
		}

		game.history += " " + res.first + "," + msg.moveTime;
		game.stime = msg.moveTime;
		game.whiteTime = msg.whiteTime;
		game.blackTime = msg.blackTime;
		return true;
	}

	@Query("SELECT * FROM " + ActiveGameEntity.TABLE_NAME + " WHERE gameid = :gameId")
	ActiveGameEntity getGame(String gameId);

	@Query("SELECT * FROM " + ActiveGameEntity.TABLE_NAME + " ORDER BY stime DESC")
	PagingSource<Integer, ActiveGameEntity> getAllGames();

	@Query("SELECT gameid FROM " + ActiveGameEntity.TABLE_NAME)
	List<String> getAllGameIds();

	@Query("SELECT COUNT(*) FROM " + ActiveGameEntity.TABLE_NAME + " WHERE opponent = "
	    + EnumsFixed.OpponentType_REMOTE_WHITE + " OR opponent = " + EnumsFixed.OpponentType_REMOTE_BLACK + " LIMIT 1")
	boolean hasInviteGame();

	@Insert
	void insert(ActiveGameEntity game);

	@Delete
	void delete(ActiveGameEntity game);

	@Update
	void update(ActiveGameEntity game);

	@Query("DELETE FROM " + ActiveGameEntity.TABLE_NAME + " WHERE gameid = :gameId")
	void deleteGame(String gameId);
}
