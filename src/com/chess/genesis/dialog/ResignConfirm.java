package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class ResignConfirm extends Dialog implements OnClickListener
{
	public final static int MSG = 107;

	private final Handler handle;

	public ResignConfirm(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Resign Confirmation");

		setContentView(R.layout.resign_confirm);

		Button button = (Button) findViewById(R.id.resign_ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.resign_cancel);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.resign_ok:
			handle.sendMessage(handle.obtainMessage(MSG, new Bundle()));
			break;
		}
		dismiss();
	}
}
