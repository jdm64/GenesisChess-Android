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
