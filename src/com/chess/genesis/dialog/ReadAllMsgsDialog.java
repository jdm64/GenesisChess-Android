package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class ReadAllMsgsDialog extends Dialog implements OnClickListener
{
	public final static int MSG  = 115;

	private final Context context;
	private final Handler handle;

	public ReadAllMsgsDialog(final Context _context, final Handler handler)
	{
		super(_context);
		context = _context;
		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Mark All Messages Read");

		setContentView(R.layout.readallmsgs);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.ok:
			final GameDataDB db = new GameDataDB(v.getContext());
			db.setAllMsgsRead();
			db.close();

			handle.sendMessage(handle.obtainMessage(MSG));
			break;
		}
		dismiss();
	}
}
