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
package com.chess.genesis.net.msgs;

import com.chess.genesis.data.Enums.*;
import com.chess.genesis.processor.*;

@ZmqMessage
public class CreateInviteMsg extends ZmqMsg
{
	public static final int ID = 7;

	public int game_type;
	public int play_as;
	public int clock_type;
	public int base_time;
	public int inc_time;

	public static CreateInviteMsg build(GameType gameType, ColorType playAs, ClockType clockType, int baseTime, int incTime)
	{
		var msg = new CreateInviteMsg();
		msg.game_type = gameType.norm().id;
		msg.play_as = playAs.norm().id;
		msg.clock_type = clockType.id;
		msg.base_time = baseTime;
		msg.inc_time = incTime;
		return msg;
	}

	@Override
	public int type()
	{
		return ID;
	}
}
