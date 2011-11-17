package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class CopyGameConfirm extends BaseDialog implements OnClickListener
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
		super.onCreate(savedInstanceState);
		setTitle("Copy Game To Local");
		setBodyView(R.layout.dialog_confirm_copygame);
		setButtonTxt(R.id.ok, "Copy");
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final GameDataDB db = new GameDataDB(v.getContext());
			db.copyGameToLocal(gameid, type);
			db.close();
		}
		dismiss();
	}
}
