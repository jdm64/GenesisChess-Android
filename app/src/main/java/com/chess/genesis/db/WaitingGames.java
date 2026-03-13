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
package com.chess.genesis.db;

import java.util.*;
import java.util.stream.*;
import android.content.*;
import com.chess.genesis.R;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;

public class WaitingGames
{
	private static final String SEPARATOR = ";";

	public static Set<WaitingData> get(Context ctx)
	{
		var result = new HashSet<WaitingData>();
		var stored = Pref.getString(ctx, R.array.pf_waitingGames);
		if (stored == null || stored.isEmpty()) {
			return result;
		}
		var items = stored.split(SEPARATOR);
		return Arrays.stream(items).filter(s -> !s.isEmpty())
		    .map(WaitingData::fromPref).collect(Collectors.toSet());
	}

	public static void remove(Context ctx, WaitingData toMatch)
	{
		Set<WaitingData> waitingSet = get(ctx);
		WaitingData toRemove = null;

		for (WaitingData waiting : waitingSet) {
			if (waiting.matches(toMatch)) {
				toRemove = waiting;
				break;
			}
		}

		if (toRemove != null) {
			waitingSet.remove(toRemove);
			save(ctx, waitingSet);
		}
	}

	public static void put(Context ctx, WaitingData data)
	{
		var set = get(ctx);
		set.add(data);
		save(ctx, set);
	}

	private static void save(Context ctx, Set<WaitingData> set)
	{
		var str = set.stream().map(WaitingData::toPref).collect(Collectors.joining(SEPARATOR));
		new PrefEdit(ctx).putString(R.array.pf_waitingGames, str).commit();
	}
}
