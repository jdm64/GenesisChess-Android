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

package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.util.AttributeSet;

class IntListPreference extends ListPreference
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
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue)
	{
		final SharedPreferences pref = getSharedPreferences();
		final int value = pref.getInt(getKey(), Integer.valueOf((String) defaultValue));

		// if index fails set to default
		if (findIndexOfValue(String.valueOf(value)) == -1)
			setValue((String) defaultValue);
		else
			setValue(String.valueOf(value));
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		if (!positiveResult)
			return;

		final Editor editor = getEditor();
		editor.putInt(getKey(), Integer.valueOf(getValue()));
		editor.commit();
	}
}
