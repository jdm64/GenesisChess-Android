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
import com.chess.genesis.util.*;

public abstract class ZmqMsg
{
	public abstract int type();

	public <T> T as(Class<T> theClass)
	{
		return theClass.cast(this);
	}

	public static ZmqMsg parse(byte[] data)
	{
		try {
			return ZmqMessageHelper.parse(data);
		} catch (IOException e) {
			Util.logErr(e, ZmqMsg.class);
			return new UnknownMsg(data);
		}
	}

	public byte[] toBytes() throws IOException
	{
		return ZmqMessageHelper.toBytes(this);
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
