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

public class InviteMsg
{
	public int type;
	public int color;

	public static InviteMsg parse(MqttMessage msg)
	{
		var invite = new InviteMsg();
		var packer = MessagePack.newDefaultUnpacker(msg.getPayload());
		try {
			invite.type = packer.unpackInt();
			invite.color = packer.unpackInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return invite;
	}

	public static byte[] write(int type, int color)
	{
		var packer = MessagePack.newDefaultBufferPacker();
		try {
			packer.packInt(type);
			packer.packInt(color);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return packer.toByteArray();
	}

	@Override
	public String toString()
	{
		return "InviteMsg{" + "type=" + type + ", color=" + color + '}';
	}
}
