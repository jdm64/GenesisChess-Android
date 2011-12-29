package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class DeleteArchiveDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 113;

	private final Handler handle;
	private final String gameid;

	public DeleteArchiveDialog(final Context context, final Handler handler, final String _gameid)
	{
		super(context);

		handle = handler;
		gameid = _gameid;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Delete Game");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.ok, "Delete Game");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.delete_archive);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final GameDataDB db = new GameDataDB(v.getContext());
			db.deleteArchiveGame(gameid);
			db.close();
			handle.sendMessage(handle.obtainMessage(MSG));
		}
		dismiss();
	}
}
