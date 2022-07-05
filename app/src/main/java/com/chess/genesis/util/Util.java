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
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import androidx.compose.runtime.*;

public class Util
{
	private final static String SUID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_.~";

	static ExecutorService pool = null;

	private Util() {}

	public synchronized static void runThread(Runnable runner)
	{
		if (pool == null) {
			pool = Executors.newCachedThreadPool();
		}
		pool.execute(runner);
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

		var last = randChar(rand, true);
		buff.append(last);
		for (int i = 0; i < 6; i++) {
			last = randChar(rand, !Character.isLetterOrDigit(last));
			buff.append(last);
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

	public static void getMetrics(DisplayMetrics dm, Context context)
	{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
	}
}
