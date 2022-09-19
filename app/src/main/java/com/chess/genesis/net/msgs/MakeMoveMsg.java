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

import java.io.*;
import org.msgpack.core.*;

public class MakeMoveMsg extends ZmqMsg
{
	public static final int ID = 11;

	public String game_id;
	public String to_move;

	public static ZmqMsg build(String gameId, String moveStr)
	{
		var msg = new MakeMoveMsg();
		msg.game_id = gameId;
		msg.to_move = moveStr;
		return msg;
	}

	@Override
	public int type()
	{
		return ID;
	}

	@Override
	ZmqMsg parse(MessageUnpacker packer) throws IOException
	{
		game_id = packer.unpackString();
		to_move = packer.unpackString();
		return this;
	}

	@Override
	void toBytes(MessageBufferPacker packer) throws IOException
	{
		packer.packString(game_id);
		packer.packString(to_move);
	}
}
