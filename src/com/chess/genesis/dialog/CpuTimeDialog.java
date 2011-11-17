package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class CpuTimeDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 110;

	private final Handler handle;
	private final int time;

	public CpuTimeDialog(final Context context, final Handler handler, final int Time)
	{
		super(context);

		handle = handler;
		time = Time;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Set CPU Time Limit");
		setBodyView(R.layout.dialog_cputime);
		setButtonTxt(R.id.ok, "Set Time");

		final NumberSpinner number = (NumberSpinner) findViewById(R.id.time);
		number.setRange(1, 30);
		number.setCurrent(time);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final NumberSpinner number = (NumberSpinner) findViewById(R.id.time);
			final Integer value = Integer.valueOf(number.getCurrent());
			handle.sendMessage(handle.obtainMessage(MSG, value));
		}
		dismiss();
	}
}
