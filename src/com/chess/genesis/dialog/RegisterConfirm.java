package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

class RegisterConfirm extends BaseDialog implements OnClickListener
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
		super.onCreate(savedInstanceState);
		setTitle("Register Confirmation");
		setBodyView(R.layout.dialog_confirm_register);
		setButtonTxt(R.id.ok, "Register");

		TextView text = (TextView) findViewById(R.id.username);
		text.setText(data.getString("username"));

		text = (TextView) findViewById(R.id.email);
		text.setText(data.getString("email"));
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG, data));
		dismiss();
	}
}
