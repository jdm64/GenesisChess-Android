package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

class StatsLookupDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 123;

	private final Handler handle;

	private EditText txtinput;

	public StatsLookupDialog(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("User Stats Lookup");
		setBodyView(R.layout.dialog_statslookup);
		setButtonTxt(R.id.ok, "Lookup");
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final EditText txt = (EditText) findViewById(R.id.username);
			final String username = txt.getText().toString().trim();

			if (username.length() >= 3)
				handle.sendMessage(handle.obtainMessage(MSG, username));
		}
		dismiss();
	}
}
