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
package com.chess.genesis.engine;

import static org.mockito.Mockito.*;
import java.util.*;
import android.os.*;
import org.mockito.stubbing.*;

public class MockParcel
{
	public static Parcel create()
	{
		Parcel mock = mock(Parcel.class);

		var queue = new ArrayDeque<>();

		doAnswer((Answer<Object>) invocation->{
			queue.add(invocation.getArgument(0));
			return null;
		}).when(mock).writeInt(anyInt());

		when(mock.readInt()).thenAnswer(i -> queue.pop());

		return mock;
	}
}
