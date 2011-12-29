package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class RegisterActivation extends BaseDialog implements OnClickListener
{
	public final static int MSG = 103;

	private final Handler handle;

	public RegisterActivation(final Context context, final Handler handler)
	{
		super(context, BaseDialog.CANCEL);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Activate Account");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.cancel, "Close");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.register_activation);
	}

	public void onClick(final View v)
	{
		handle.sendMessage(handle.obtainMessage(MSG));

		dismiss();
	}
}
