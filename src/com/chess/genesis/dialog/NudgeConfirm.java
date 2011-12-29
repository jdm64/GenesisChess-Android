package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class NudgeConfirm extends BaseDialog implements OnClickListener
{
	public final static int MSG = 116;

	private final Handler handle;
	private final String gameid;

	public NudgeConfirm(final Context context, final Handler handler)
	{
		this(context, handler, "");
	}

	public NudgeConfirm(final Context context, final Handler handler, final String GameID)
	{
		super(context);

		handle = handler;
		gameid = GameID;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Nudge Confirmation");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.ok, "Nudge");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.nudge_confirm);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG, gameid));
		dismiss();
	}
}
