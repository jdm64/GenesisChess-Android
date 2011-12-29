package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class ReadAllMsgsDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG  = 115;

	private final Handler handle;

	public ReadAllMsgsDialog(final Context context, final Handler handler)
	{
		super(context);
		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Mark All Messages Read");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.ok, "Mark Read");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.readallmsgs);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final GameDataDB db = new GameDataDB(v.getContext());
			db.setAllMsgsRead();
			db.close();

			handle.sendMessage(handle.obtainMessage(MSG));
		}
		dismiss();
	}
}
