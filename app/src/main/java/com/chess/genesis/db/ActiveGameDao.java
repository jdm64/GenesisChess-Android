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

import java.security.*;
import java.util.*;
import android.content.*;
import android.util.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
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
		var type = data.getType().getValue();
		var opp = data.getOpp().getValue();
		var color = data.getColor().getValue();

		if (color == Enums.RANDOM_OPP)
			color = new SecureRandom().nextBoolean() ? Enums.WHITE_OPP : Enums.BLACK_OPP;

		switch (opp) {
		case Enums.CPU_OPPONENT:
			opp = color == Enums.WHITE_OPP ? Enums.CPU_WHITE_OPPONENT : Enums.CPU_BLACK_OPPONENT;
			break;
		case Enums.HUMAN_OPPONENT:
			break;
		default:
			throw new IllegalStateException("Unexpected opponent type: " + opp);
		}

		return newLocalGame("Untitled", type, opp);
	}

	default ActiveGameEntity newLocalGame(String gamename, int gametype, int opponent)
	{
		var game = new ActiveGameEntity();
		game.gameid = UUID.randomUUID().toString();
		game.name = gamename;
		game.gametype = gametype;
		game.opponent = opponent;
		game.zfen = (gametype == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard()).printZFen();
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
		game.name = "Invite " + Enums.GameType(gameType) + "; Play " + (color == Piece.WHITE ? "white" : "black");
		game.gametype = gameType;
		game.opponent = color == Piece.WHITE ? Enums.INVITE_BLACK_OPPONENT : Enums.INVITE_WHITE_OPPONENT;
		game.zfen = (gameType == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard()).printZFen();
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
		game.ctime = msg.create_time;
		game.stime = msg.save_time;
		game.name = name;
		game.white = msg.white;
		game.black = msg.black;
		game.gametype = msg.game_type;
		game.opponent = msg.getOpponentType(ctx);
		game.history = msg.movesString();
		game.zfen = (game.gametype == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard()).printZFen();

		insert(game);

		return game;
	}

	default List<Pair<String,Integer>> updateInviteGame(ActiveGameDataMsg msg, Context ctx)
	{
		var game = getGame(msg.game_id);
		if (game == null) {
			return Collections.emptyList();
		}

		game.stime = msg.save_time;
		game.white = msg.white;
		game.black = msg.black;
		game.history = msg.movesString();

		var newMoves = new ArrayList<Pair<String,Integer>>();
		var moves = game.history.split(" +");
		for (int i = moves.length - 1; i < msg.moves.size(); i++) {
			newMoves.add(new Pair<>(msg.moves.get(i).first, i));
		}

		update(game);

		return newMoves;
	}

	default boolean saveMove(String gameId, int index, String move)
	{
		var game = getGame(gameId);
		if (game == null) {
			return false;
		}

		var board = game.gametype == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard();
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
		var game = getGame(msg.game_id);
		if (game == null) {
			return false;
		}

		var board = game.gametype == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard();
		if (!board.parseZFen(game.zfen)) {
			return false;
		} else if (board.getPly() != msg.move_idx) {
			return false;
		}

		var res = board.parseMove(msg.move_str);
		if (res.second != Board.VALID_MOVE) {
			return false;
		}

		game.history += " " + res.first + "," + msg.move_time;
		game.stime = msg.move_time;

		update(game);
		return true;
	}

	@Query("SELECT * FROM " + ActiveGameEntity.TABLE_NAME + " WHERE gameid = :gameId")
	ActiveGameEntity getGame(String gameId);

	@Query("SELECT * FROM " + ActiveGameEntity.TABLE_NAME + " ORDER BY stime DESC")
	PagingSource<Integer, ActiveGameEntity> getAllGames();

	@Query("SELECT COUNT(*) FROM " + ActiveGameEntity.TABLE_NAME + " WHERE opponent = "
	    + Enums.INVITE_WHITE_OPPONENT + " OR opponent = " + Enums.INVITE_BLACK_OPPONENT + " LIMIT 1")
	boolean hasInviteGame();

	@Insert
	void insert(ActiveGameEntity game);

	@Delete
	void delete(ActiveGameEntity game);

	@Update
	void update(ActiveGameEntity game);
}
