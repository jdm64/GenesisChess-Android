/* GenesisChess, an Android chess application
 * Copyright 2026, Justin Madru (justin.jdm64@gmail.com)
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
package com.chess.genesis.api;

import com.chess.genesis.data.Enums.*;
import com.chess.genesis.net.msgs.*;

public record WaitingData(int gameType, int playAs, int baseTime, int incTime)
{
	public boolean matches(WaitingData other)
	{
		if (baseTime != other.baseTime || incTime != other.incTime) {
			return false;
		}
		if (gameType != GameType.ANY.id && gameType != other.gameType) {
			return false;
		}
		return playAs == ColorType.RANDOM.id || playAs == other.playAs;
	}

	public String toPref()
	{
		return gameType + ":" + playAs + ":" + baseTime + ":" + incTime;
	}

	public static WaitingData fromPref(String str)
	{
		var parts = str.split(":");
		return new WaitingData(
		    Integer.parseInt(parts[0]),
		    Integer.parseInt(parts[1]),
		    Integer.parseInt(parts[2]),
		    Integer.parseInt(parts[3]));
	}
}