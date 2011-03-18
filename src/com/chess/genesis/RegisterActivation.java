package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RegisterActivation extends Dialog implements OnClickListener
{
	public final static int MSG = 103;

	private Handler handle;

	public RegisterActivation(Context context, Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setTitle("Activate Account");

		setContentView(R.layout.register_activation);

		Button close = (Button) findViewById(R.id.close);
		close.setOnClickListener(this);
	}

	public void onClick(View v)
	{
		handle.sendMessage(handle.obtainMessage(MSG, new Bundle()));

		dismiss();
	}
}
