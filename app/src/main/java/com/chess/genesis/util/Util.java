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
package com.chess.genesis.util;

import java.security.*;
import java.util.concurrent.*;
import android.os.*;
import androidx.compose.runtime.*;

public class Util
{
	private final static String SUID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_.~";

	private Util() {}

	public static void runThread(Runnable runner)
	{
		Executors.newSingleThreadExecutor().execute(runner);
	}

	public static void runUI(Runnable runner)
	{
		new Handler(Looper.getMainLooper()).post(runner);
	}

	public static <T> MutableState<T> getState(T val)
	{
		return SnapshotStateKt.mutableStateOf(val, SnapshotStateKt.structuralEqualityPolicy());
	}

	public static String getSUID()
	{
		var rand = new SecureRandom();
		var buff = new StringBuilder();

		buff.append(randChar(rand, true));
		for (int i = 0; i < 6; i++) {
			buff.append(randChar(rand, false));
		}
		buff.append(randChar(rand, true));
		return buff.toString();
	}

	private static char randChar(SecureRandom rand, boolean onlyAlphNum)
	{
		char c;
		do {
			c = SUID_CHARS.charAt(Math.abs(rand.nextInt() % SUID_CHARS.length()));
		} while (onlyAlphNum && !Character.isLetterOrDigit(c));
		return c;
	}
}
