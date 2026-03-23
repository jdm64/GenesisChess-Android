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
package com.chess.genesis.api;

import com.chess.genesis.data.Enums.ClockType;
import com.chess.genesis.data.Enums.GameStatus;
import com.chess.genesis.engine.Piece;

public record StmState(String white, String black, int yourColor, int stm, GameStatus status, ClockType type, long lastMove, long whiteTime, long blackTime)
{
	public boolean hasClock()
	{
		return type != ClockType.NO_CLOCK;
	}

	public boolean clockRunning(int sideColor)
	{
		return hasClock() && lastMove > 0 && sideColor == stm && status.isGameActive();
	}

	public long remaining(boolean isWhite)
	{
		var playerTime = isWhite ? whiteTime : blackTime;
		var sideColor = isWhite ? Piece.WHITE : Piece.BLACK;

		if (clockRunning(sideColor)) {
			var timeElapsed = System.currentTimeMillis() - lastMove;
			return playerTime - timeElapsed;
		}
		return playerTime;
	}

	public boolean isTimeout()
	{
		if (hasClock()) {
			return remaining(stm == Piece.WHITE) < 0;
		}
		return false;
	}

	public int delay()
	{
		if (hasClock()) {
			return remaining(stm == Piece.WHITE) < 10000 ? 25 : 250;
		}
		return 1000;
	}
}
