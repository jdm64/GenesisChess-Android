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

import androidx.room.*;
import com.chess.genesis.api.*;
import com.chess.genesis.data.Enums.*;

@Entity(tableName = ArchiveGameEntity.TABLE_NAME)
public class ArchiveGameEntity extends GameEntity
{
	public static final String TABLE_NAME = "archive_games";

	public double whiteRatingBefore;

	public double whiteRatingAfter;

	public double blackRatingBefore;

	public double blackRatingAfter;

	@Override
	public GameSource getSource()
	{
		return GameSource.ARCHIVE;
	}

	@Override
	public RatingsData getRatings()
	{
		if (whiteRatingBefore == 0) {
			return null;
		}
		return new RatingsData(whiteRatingBefore, whiteRatingAfter, blackRatingBefore, blackRatingAfter);
	}
}
