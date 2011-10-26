package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class RegisterActivation extends Dialog implements OnClickListener
{
	public final static int MSG = 103;

	private final Handler handle;

	public RegisterActivation(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Activate Account");

		setContentView(R.layout.register_activation);

		final Button close = (Button) findViewById(R.id.close);
		close.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		handle.sendMessage(handle.obtainMessage(MSG, new Bundle()));

		dismiss();
	}
}
