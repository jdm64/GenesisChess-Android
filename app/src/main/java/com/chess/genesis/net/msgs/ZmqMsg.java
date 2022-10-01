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

public abstract class ZmqMsg
{
	public abstract int type();

	abstract ZmqMsg parse(MessageUnpacker packer) throws IOException;

	abstract void toBytes(MessageBufferPacker packer) throws IOException;

	public <T> T as(Class<T> theClass)
	{
		return theClass.cast(this);
	}

	public static ZmqMsg parse(byte[] data)
	{
		var packer = MessagePack.newDefaultUnpacker(data);
		try {
			var msgType = packer.unpackInt();

			switch (msgType) {
			case ActiveGameDataMsg.ID:
				return new ActiveGameDataMsg().parse(packer);
			case AnonAcctMsg.ID:
				return new AnonAcctMsg().parse(packer);
			case CreateInviteMsg.ID:
				return new CreateInviteMsg().parse(packer);
			case ErrorMsg.ID:
				return new ErrorMsg().parse(packer);
			case GetActiveDataMsg.ID:
				return new GetActiveDataMsg().parse(packer);
			case JoinInviteMsg.ID:
				return new JoinInviteMsg().parse(packer);
			case LastMoveMsg.ID:
				return new LastMoveMsg().parse(packer);
			case LoginMsg.ID:
				return new LoginMsg().parse(packer);
			case LoginResultMsg.ID:
				return new LoginResultMsg().parse(packer);
			case MakeMoveMsg.ID:
				return new MakeMoveMsg().parse(packer);
			case OkMsg.ID:
				return new OkMsg().parse(packer);
			case PingMsg.ID:
				return new PingMsg().parse(packer);
			case PongMsg.ID:
				return new PongMsg().parse(packer);
			case RegisterAnonMsg.ID:
				return new RegisterAnonMsg().parse(packer);
			case RegisterMsg.ID:
				return new RegisterMsg().parse(packer);
			default:
				return new UnknownMsg(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new UnknownMsg(data);
		}
	}

	public byte[] toBytes() throws IOException
	{
		var packer = MessagePack.newDefaultBufferPacker();
		packer.packInt(type());
		toBytes(packer);
		return packer.toByteArray();
	}

	@Override
	public String toString()
	{
		var buff = new StringBuilder();
		var clz = getClass();
		buff.append(clz.getSimpleName());
		buff.append("{");
		for (var field : clz.getFields()) {
			buff.append(field.getName());
			buff.append("=");
			try {
				buff.append(field.get(this));
			} catch (Throwable e) {
				e.printStackTrace();
			}
			buff.append(", ");
		}
		buff.append("}");
		return buff.toString();
	}
}
