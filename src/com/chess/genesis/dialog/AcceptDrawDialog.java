package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class AcceptDrawDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 119;

	private final Handler handle;

	public AcceptDrawDialog(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Accept Draw?");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.ok, "Accept");
		setButtonTxt(R.id.cancel, "Decline");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.draw_accept);
	}

	public void onClick(final View v)
	{
		final String value = (v.getId() == R.id.ok)? "offer" : "decline";
		handle.sendMessage(handle.obtainMessage(MSG, value));

		dismiss();
	}
}
