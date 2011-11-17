package com.chess.genesis;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.util.AttributeSet;

class VibratePreference extends ListPreference implements DialogInterface.OnClickListener
{
	private Context context;
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

	private long[] parseVibrate(final String pattern)
	{
		final String[] arr = pattern.trim().split(",");
		final long[] vib = new long[arr.length];

		for (int i = 0; i < arr.length; i++)
			vib[i] = Long.valueOf(arr[i]);
		return vib;
	}
}
