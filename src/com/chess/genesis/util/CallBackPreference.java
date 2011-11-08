package com.chess.genesis;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

class CallBackPreference extends DialogPreference
{
	public interface CallBack
	{
		void runCallBack(final CallBackPreference preference, final boolean result);
	}

	private CallBack action;
 
	public CallBackPreference(final Context context)
	{
		this(context, null);
	}

	public CallBackPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		setPersistent(false);
		action = null;
	}

	public void setCallBack(final CallBack callback)
	{
		action = callback;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		if (action == null)
			return;
		action.runCallBack(this, positiveResult);
	}
}
