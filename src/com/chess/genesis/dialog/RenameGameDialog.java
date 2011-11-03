package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView.BufferType;

class RenameGameDialog extends Dialog implements OnClickListener
{
	public final static int MSG = 114;

	private final Handler handle;
	private final String gamename;
	private final int gameid;

	private EditText txtinput;

	public RenameGameDialog(final Context context, final Handler handler, final int id, final String name)
	{
		super(context);

		handle = handler;
		gameid = id;
		gamename = name;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Rename Game");

		setContentView(R.layout.renamegame);

		txtinput = (EditText) findViewById(R.id.game_name_input);
		txtinput.setText(gamename, BufferType.EDITABLE);

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
			db.renameLocalGame(gameid, txtinput.getText().toString().trim());
			db.close();
			handle.sendMessage(handle.obtainMessage(MSG));
			break;
		}
		dismiss();
	}
}
