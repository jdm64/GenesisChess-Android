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

public class ClockState
{
	public final ClockType type;
	public final long lastMove;
	public final long whiteTime;
	public final long blackTime;
	public final int stm;

	public ClockState(ClockType type, long lastMove, long whiteTime, long blackTime, int stm)
	{
		this.type = type;
		this.lastMove = lastMove;
		this.whiteTime = whiteTime;
		this.blackTime = blackTime;
		this.stm = stm;
	}
}
