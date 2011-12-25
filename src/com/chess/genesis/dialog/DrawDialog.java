package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class DrawDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 120;

	private final Handler handle;

	public DrawDialog(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Send Draw");
		setBodyView(R.layout.dialog_draw_send);
		setButtonTxt(R.id.ok, "Send");
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG, "offer"));

		dismiss();
	}
}
