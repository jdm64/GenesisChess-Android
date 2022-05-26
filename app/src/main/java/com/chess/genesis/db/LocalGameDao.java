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
import com.chess.genesis.engine.*;
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
		var opp = data.getCpu().getValue();
		return newLocalGame(name, type, opp);
	}

	default LocalGameEntity newLocalGame(String gamename, int gametype, int opponent)
	{
		var game = new LocalGameEntity();
		game.gameid = UUID.randomUUID().toString();
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
