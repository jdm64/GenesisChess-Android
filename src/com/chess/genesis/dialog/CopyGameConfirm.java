package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class CopyGameConfirm extends Dialog implements OnClickListener
{
	private final String gameid;
	private final int type;

	public CopyGameConfirm(final Context context, final String _gameid, final int _type)
	{
		super(context);

		gameid = _gameid;
		type = _type;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Copy Game To Local");

		setContentView(R.layout.dialog_confirm_copygame);

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
			db.copyGameToLocal(gameid, type);
			db.close();
			break;
		}
		dismiss();
	}
}
