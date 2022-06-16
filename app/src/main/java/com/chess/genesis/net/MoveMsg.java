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
package com.chess.genesis.net;

import java.io.*;
import org.eclipse.paho.client.mqttv3.*;
import org.msgpack.core.*;
import com.chess.genesis.engine.*;

public class MoveMsg
{
	public int index;
	public String move;

	public static MoveMsg parse(MqttMessage msg)
	{
		var move = new MoveMsg();
		var packer = MessagePack.newDefaultUnpacker(msg.getPayload());
		try {
			move.index = packer.unpackInt();
			move.move = packer.unpackString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return move;
	}

	public static byte[] write(Move move, int index)
	{
		var packer = MessagePack.newDefaultBufferPacker();
		try {
			packer.packInt(index);
			packer.packString(move.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return packer.toByteArray();
	}

	@Override
	public String toString()
	{
		return "MoveMsg{" + "index=" + index + ", move='" + move + '\'' + '}';
	}
}
