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
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;
import androidx.paging.*;
import androidx.room.*;

@Dao
public interface LocalGameDao
{
	static LocalGameDao get(Context context)
	{
		return GameDatabase.getInstance(context).getLocalGameDao();
	}

	default LocalGameEntity newLocalGame(NewGameState data)
	{
		var name = data.getName().getValue();
		var type = data.getType().getValue();
		var opp = data.getOpp().getValue();
		var color = data.getColor().getValue();

		if (color == Enums.RANDOM_OPP)
			color = new SecureRandom().nextBoolean() ? Enums.WHITE_OPP : Enums.BLACK_OPP;

		switch (opp) {
		case Enums.CPU_OPPONENT:
			opp = color == Enums.WHITE_OPP ? Enums.CPU_BLACK_OPPONENT : Enums.CPU_WHITE_OPPONENT;
			break;
		case Enums.INVITE_OPPONENT:
			opp = color == Enums.WHITE_OPP ? Enums.INVITE_WHITE_OPPONENT : Enums.INVITE_BLACK_OPPONENT;
		case Enums.HUMAN_OPPONENT:
		default:
			// do nothing
		}

		return newLocalGame(name, type, opp);
	}

	default LocalGameEntity newLocalGame(String gamename, int gametype, int opponent)
	{
		var game = new LocalGameEntity();
		game.gameid = (opponent == Enums.INVITE_WHITE_OPPONENT || opponent == Enums.INVITE_BLACK_OPPONENT) ?
			Util.getSUID() : UUID.randomUUID().toString();
		game.name = gamename;
		game.gametype = gametype;
		game.opponent = opponent;
		game.zfen = (gametype == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard()).printZfen();
		game.history = "";
		game.ctime = System.currentTimeMillis();
		game.stime = System.currentTimeMillis();

		insert(game);

		return game;
	}

	default LocalGameEntity importInviteGame(String gameId, int gameType, int color)
	{
		var game = new LocalGameEntity();
		game.gameid = gameId;
		game.name = "Invite " + Enums.GameType(gameType) + "; Play " + (color == Piece.WHITE ? "white" : "black");
		game.gametype = gameType;
		game.opponent = color == Piece.WHITE ? Enums.INVITE_BLACK_OPPONENT : Enums.INVITE_WHITE_OPPONENT;
		game.zfen = (gameType == Enums.GENESIS_CHESS ? new GenBoard() : new RegBoard()).printZfen();
		game.history = "";
		game.ctime = System.currentTimeMillis();
		game.stime = System.currentTimeMillis();

		insert(game);

		return game;
	}

	@Query("SELECT * FROM local_games WHERE gameid = :gameId")
	LocalGameEntity getGame(String gameId);

	@Query("SELECT * FROM local_games ORDER BY stime DESC")
	PagingSource<Integer, LocalGameEntity> getAllGames();

	@Insert
	void insert(LocalGameEntity game);

	@Delete
	void delete(LocalGameEntity game);

	@Update
	void update(LocalGameEntity game);
}
