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

import android.content.*;
import androidx.room.*;

@Database(
    version = 1,
    entities = {LocalGameEntity.class, OnlineGameEntity.class, ArchiveGameEntity.class, MsgEntity.class}
)
public abstract class GameDatabase extends RoomDatabase
{
	private static GameDatabase instance;

	public static GameDatabase getInstance(Context context)
	{
		if (instance == null) {
			instance = Room.databaseBuilder(context.getApplicationContext(), GameDatabase.class,"gamedb").build();
		}
		return instance;
	}

	public abstract LocalGameDao getLocalGameDao();

	public abstract OnlineGameDao getOnlineGameDao();

	public abstract ArchiveGameDao getArchiveGameDao();

	public abstract MsgsDao getMsgsDao();
}
