package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class DeleteLocalDialog extends Dialog implements OnClickListener
{
	private final int gameid;

	public DeleteLocalDialog(final Context context, final int _gameid)
	{
		super(context);

		gameid = _gameid;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Delete Game");

		setContentView(R.layout.deletelocal);

		Button button = (Button) findViewById(R.id.delete_cancel);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.delete_ok);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.delete_ok:
			final GameDataDB db = new GameDataDB(v.getContext());
			db.deleteLocalGame(gameid);
			db.close();
			GameList.self.gamelist_adapter.update();
			break;
		}
		dismiss();
	}
}
