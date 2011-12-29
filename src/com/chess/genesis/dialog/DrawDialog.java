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
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.ok, "Send");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.draw_send);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG, "offer"));

		dismiss();
	}
}
