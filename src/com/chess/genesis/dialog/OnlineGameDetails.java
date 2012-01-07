package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

class OnlineGameDetails extends BaseDialog implements OnClickListener
{
	private final Bundle gamedata;

	public OnlineGameDetails(final Context context, final Bundle data)
	{
		super(context, BaseDialog.CANCEL);

		gamedata = data;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Online Game Details");
		setBodyView(R.layout.dialog_gamedetails_online);
		setButtonTxt(R.id.cancel, "Close");

		RobotoText txt = (RobotoText) findViewById(R.id.white);
		txt.setText(gamedata.getString("white"));
		txt = (RobotoText) findViewById(R.id.black);
		txt.setText(gamedata.getString("black"));

		txt = (RobotoText) findViewById(R.id.gametype);
		txt.setText(Enums.GameType(Integer.valueOf(gamedata.getString("gametype"))));
		txt = (RobotoText) findViewById(R.id.eventtype);
		txt.setText(Enums.EventType(Integer.valueOf(gamedata.getString("eventtype"))));
		txt = (RobotoText) findViewById(R.id.status);
		txt.setText(Enums.GameStatus(Integer.valueOf(gamedata.getString("status"))));

		txt = (RobotoText) findViewById(R.id.ctime);
		txt.setText((new PrettyDate(gamedata.getString("ctime"))).agoFormat());
		txt = (RobotoText) findViewById(R.id.stime);
		txt.setText((new PrettyDate(gamedata.getString("stime"))).agoFormat());

		txt = (RobotoText) findViewById(R.id.zfen);
		txt.setText(gamedata.getString("zfen"));
		txt = (RobotoText) findViewById(R.id.history);
		txt.setText(gamedata.getString("history"));
	}

	public void onClick(final View v)
	{
		dismiss();
	}
}
