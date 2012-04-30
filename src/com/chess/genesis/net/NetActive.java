/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.net;

import java.util.concurrent.atomic.*;

public final class NetActive
{
	private final static AtomicInteger active = new AtomicInteger(0);

	private NetActive()
	{
	}

	public static int get()
	{
		return active.get();
	}

	public static void inc()
	{
		active.incrementAndGet();
	}

	public static void dec()
	{
		if (active.decrementAndGet() < 1)
			new Thread(new NetDisconnect()).start();
	}
}
