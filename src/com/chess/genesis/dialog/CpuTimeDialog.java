package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

class CpuTimeDialog extends Dialog implements OnClickListener
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
		setTitle("Set CPU Time Limit");

		setContentView(R.layout.cputime_dialog);

		final NumberSpinner number = (NumberSpinner) findViewById(R.id.time);
		number.setRange(1, 30);
		number.setCurrent(time);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.ok:
			final NumberSpinner number = (NumberSpinner) findViewById(R.id.time);
			final Integer value = Integer.valueOf(number.getCurrent());
			handle.sendMessage(handle.obtainMessage(MSG, value));
			break;
		}
		dismiss();
	}
}
