package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class DeleteLocalDialog extends Dialog implements OnClickListener
{
	public final static int MSG = 112;

	private final Handler handle;
	private final int gameid;

	public DeleteLocalDialog(final Context context, final Handler handler, final int _gameid)
	{
		super(context);

		handle = handler;
		gameid = _gameid;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Delete Game");

		setContentView(R.layout.dialog_delete_local);

		Button button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.ok:
			final GameDataDB db = new GameDataDB(v.getContext());
			db.deleteLocalGame(gameid);
			db.close();
			handle.sendMessage(handle.obtainMessage(MSG));
			break;
		}
		dismiss();
	}
}
