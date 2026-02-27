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
import androidx.paging.*;
import androidx.room.*;
import com.chess.genesis.data.Enums.*;
import com.chess.genesis.net.msgs.*;

@Dao
public interface ArchiveGameDao
{
	static ArchiveGameDao get(Context context)
	{
		return GameDatabase.getInstance(context).archiveGameDao();
	}

	default boolean copyFromActive(GameResultMsg msg, Context ctx)
	{
		var actDao = ActiveGameDao.get(ctx);
		var activeGame = actDao.getGame(msg.id);
		if (activeGame == null) {
			return false;
		}

		var lastMove = msg.toLastMoveMsg();
		if (lastMove != null) {
			ActiveGameDao.saveMove(activeGame, lastMove);
		} else {
			activeGame.status = msg.status;
			activeGame.stime = msg.saveTime;
			activeGame.whiteTime = msg.whiteTime;
			activeGame.blackTime = msg.blackTime;
		}

		activeGame.hasArchiveData = true;
		actDao.update(activeGame);

		var archiveGame = new ArchiveGameEntity();
		archiveGame.opponent = OpponentType.ARCHIVED.id;

		archiveGame.gameid = activeGame.gameid;
		archiveGame.name = activeGame.name;
		archiveGame.eventType = activeGame.eventType;
		archiveGame.gametype = activeGame.gametype;
		archiveGame.clockType = activeGame.clockType;
		archiveGame.baseTime = activeGame.baseTime;
		archiveGame.incTime = activeGame.incTime;
		archiveGame.ctime = activeGame.ctime;
		archiveGame.zfen = activeGame.zfen;
		archiveGame.history = activeGame.history;
		archiveGame.white = activeGame.white;
		archiveGame.black = activeGame.black;

		archiveGame.status = msg.status;
		archiveGame.stime = msg.saveTime;
		archiveGame.whiteTime = msg.whiteTime;
		archiveGame.blackTime = msg.blackTime;
		archiveGame.whiteRatingBefore = msg.whiteRating.first;
		archiveGame.whiteRatingAfter = msg.whiteRating.second;
		archiveGame.blackRatingBefore = msg.blackRating.first;
		archiveGame.blackRatingAfter = msg.blackRating.second;

		insert(archiveGame);
		return true;
	}

	default ArchiveGameEntity update(ArchiveGameDataMsg msg)
	{
		var existing = getGame(msg.game_id);
		var entity = existing != null ? existing : new ArchiveGameEntity();

		entity.opponent = OpponentType.ARCHIVED.id;
		entity.gameid = msg.game_id;
		entity.name = msg.game_id;
		entity.eventType = msg.event_type;
		entity.gametype = msg.game_type;
		entity.status = msg.status;
		entity.clockType = msg.clock_type;
		entity.ctime = msg.create_time;
		entity.stime = msg.save_time;
		entity.baseTime = msg.base_time;
		entity.incTime = msg.inc_time;
		entity.whiteTime = msg.white_time;
		entity.blackTime = msg.black_time;
		entity.white = msg.white;
		entity.black = msg.black;
		entity.zfen = msg.zfen;
		entity.history = msg.movesString();
		entity.whiteRatingBefore = msg.whiteRating.first;
		entity.whiteRatingAfter = msg.whiteRating.second;
		entity.blackRatingBefore = msg.blackRating.first;
		entity.blackRatingAfter = msg.blackRating.second;

		if (existing != null) {
			update(entity);
		} else {
			insert(entity);
		}

		return entity;
	}

	@Query("SELECT * FROM " + ArchiveGameEntity.TABLE_NAME + " WHERE gameid = :gameId")
	ArchiveGameEntity getGame(String gameId);

	@Query("SELECT * FROM " + ArchiveGameEntity.TABLE_NAME + " ORDER BY stime DESC")
	PagingSource<Integer, ArchiveGameEntity> getAllGames();

	@Query("SELECT gameid FROM " + ArchiveGameEntity.TABLE_NAME)
	List<String> getAllGameIds();

	@Insert
	void insert(ArchiveGameEntity game);

	@Update
	void update(ArchiveGameEntity game);
}
