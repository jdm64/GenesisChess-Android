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

import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;
import androidx.annotation.*;
import androidx.room.*;

@Entity
public class GameEntity
{
	@PrimaryKey
	@NonNull
	public String gameid;

	public long ctime;

	@ColumnInfo(defaultValue = "0")
	public long stime;

	public int gametype;

	@ColumnInfo(defaultValue = " ")
	public String zfen;

	@ColumnInfo(defaultValue = " ")
	public String history;

	public String lastMoveTo()
	{
		var moves = history != null ? history.split(" +") : new String[]{""};
		var move = gametype == Enums.GENESIS_CHESS ? new GenMove() : new RegMove();
		if (!move.parse(moves[moves.length - 1]))
			return "xx";
		if (move.getCastle() != 0)
			return "oo";
		return Move.printSq(move.to);
	}
}
