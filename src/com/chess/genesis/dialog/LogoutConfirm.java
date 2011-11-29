package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class LogoutConfirm extends BaseDialog implements OnClickListener
{
	public final static int MSG = 105;

	private final Handler handle;

	public LogoutConfirm(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Logout Confirmation");
		setBodyView(R.layout.dialog_confirm_logout);
		setButtonTxt(R.id.ok, "Logout");
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG));
		dismiss();
	}
}
