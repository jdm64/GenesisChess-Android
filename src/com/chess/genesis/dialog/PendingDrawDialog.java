package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

class PendingDrawDialog extends BaseDialog implements OnClickListener
{
	public PendingDrawDialog(final Context context)
	{
		super(context, BaseDialog.CANCEL);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Pending Draw");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.cancel, "Close");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.draw_pending);
	}

	public void onClick(final View v)
	{
		dismiss();
	}
}
