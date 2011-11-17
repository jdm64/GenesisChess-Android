package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class ResignConfirm extends BaseDialog implements OnClickListener
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
		super.onCreate(savedInstanceState);
		setTitle("Resign Confirmation");
		setBodyView(R.layout.dialog_confirm_resign);
		setButtonTxt(R.id.ok, "Resign");
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG));
		dismiss();
	}
}
