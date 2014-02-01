/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
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

import android.app.AlertDialog.Builder;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.util.*;

class VibratePreference extends ListPreference
{
	private final Context context;
	private int EntryIndex;

	public VibratePreference(final Context _context)
	{
		this(_context, null);
	}

	public VibratePreference(final Context _context, final AttributeSet attrs)
	{
		super(_context, attrs);
		context = _context;
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder)
	{
		super.onPrepareDialogBuilder(builder);

		builder.setSingleChoiceItems(getEntries(), findIndexOfValue(getValue()), this);
		builder.setPositiveButton("Ok", null);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which)
	{
		EntryIndex = which;

		if (EntryIndex < 0)
			return;

		final String pattern = (String) getEntryValues()[EntryIndex];
		final Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		v.vibrate(parseVibrate(pattern), -1);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		if (EntryIndex < 0)
			return;
		setValue((String) getEntryValues()[EntryIndex]);
	}

	private static long[] parseVibrate(final String pattern)
	{
		final String[] arr = pattern.trim().split(",");
		final long[] vib = new long[arr.length];

		for (int i = 0; i < arr.length; i++)
			vib[i] = Long.parseLong(arr[i]);
		return vib;
	}
}
