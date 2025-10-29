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
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.WindowManager.*;
import android.widget.*;
import com.chess.genesis.R;
import com.chess.genesis.data.*;
import androidx.compose.runtime.*;

public class Util
{
	private final static String SUID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_.~";

	static ExecutorService pool = null;

	private Util() {}

	public static void logErr(String msg, Object cls)
	{
		Log.e(cls.getClass().getSimpleName(), msg);
	}

	public static void logErr(Throwable t, Object cls)
	{
		Log.e(cls.getClass().getSimpleName(), t.getMessage(), t);
	}

	public static void log(String msg, Object cls)
	{
		Log.i(cls.getClass().getSimpleName(), msg);
	}

	public static void log(Throwable t, Object cls)
	{
		Log.i(cls.getClass().getSimpleName(), t.getMessage(), t);
	}

	public synchronized static Future<?> runThread(Runnable runner)
	{
		if (pool == null) {
			pool = Executors.newCachedThreadPool();
		}
		return pool.submit(runner);
	}

	public static void runUI(Runnable runner)
	{
		new Handler(Looper.getMainLooper()).post(runner);
	}

	public static <T> MutableState<T> getState(T val)
	{
		return SnapshotStateKt.mutableStateOf(val, SnapshotStateKt.structuralEqualityPolicy());
	}

	public static void showToast(String txt, Context ctx)
	{
		runUI(() -> Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show());
	}

	public static String getSUID(int size)
	{
		var rand = new SecureRandom();
		var buff = new StringBuilder();

		var last = randChar(rand, true);
		buff.append(last);
		for (int i = 0; i < size - 2; i++) {
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

	public static void setScreenOnFlag(Context ctx, boolean toggle)
	{
		var flag = LayoutParams.FLAG_KEEP_SCREEN_ON;
		var window = getActivity(ctx).getWindow();
		if (toggle) {
			var screenOn = Pref.getBool(ctx, R.array.pf_screenAlwaysOn);
			window.setFlags(screenOn ? flag : 0, flag);
		} else {
			window.setFlags(0, flag);
		}
	}

	private static Activity getActivity(Context context) {
		if (context == null) return null;
		if (context instanceof Activity) return (Activity) context;
		if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());
		return null;
	}
}
