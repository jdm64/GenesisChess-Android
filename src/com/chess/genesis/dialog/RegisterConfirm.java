package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

class RegisterConfirm extends Dialog implements OnClickListener
{
	public final static int MSG = 106;

	private final Handler handle;
	private final Bundle data;

	public RegisterConfirm(final Context context, final Handler handler, final Bundle bundle)
	{
		super(context);

		handle = handler;
		data = bundle;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Register Confirmation");

		setContentView(R.layout.register_confirm);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(this);

		TextView text = (TextView) findViewById(R.id.username);
		text.setText(data.getString("username"));

		text = (TextView) findViewById(R.id.email);
		text.setText(data.getString("email"));
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.ok:
			handle.sendMessage(handle.obtainMessage(MSG, data));
			break;
		}
		dismiss();
	}
}
