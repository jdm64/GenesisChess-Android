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

import android.content.*;
import android.content.SharedPreferences.*;
import android.util.*;
import androidx.preference.*;
import androidx.preference.Preference.*;

public class IntListPreference extends ListPreference implements OnPreferenceChangeListener
{
	public IntListPreference(final Context context)
	{
		this(context, null);
	}

	public IntListPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		setPersistent(false);
	}

	@Override
	protected void onSetInitialValue(Object defaultValue)
	{
		final SharedPreferences pref = getSharedPreferences();
		final int value = pref.getInt(getKey(), Integer.parseInt((String) defaultValue));

		// if index fails set to default
		if (findIndexOfValue(String.valueOf(value)) == -1)
			setValue((String) defaultValue);
		else
			setValue(String.valueOf(value));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		final Editor editor = getSharedPreferences().edit();
		editor.putInt(getKey(), Integer.parseInt(getValue()));
		editor.apply();
		return true;
	}
}
