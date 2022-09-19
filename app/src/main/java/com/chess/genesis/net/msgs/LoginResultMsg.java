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

public class LoginResultMsg extends ZmqMsg
{
	public static final int ID = 15;

	public boolean is_ok;
	public String name;
	public String msg;

	@Override
	public int type()
	{
		return ID;
	}

	@Override
	ZmqMsg parse(MessageUnpacker packer) throws IOException
	{
		is_ok = packer.unpackBoolean();
		name = packer.unpackString();
		msg = packer.unpackString();
		return this;
	}

	@Override
	void toBytes(MessageBufferPacker packer) throws IOException
	{
		packer.packBoolean(is_ok);
		packer.packString(name);
		packer.packString(msg);
	}
}
