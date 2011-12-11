package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

class NewOnlineGameDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 100;

	private final Handler handle;

	public NewOnlineGameDialog(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("New Online Game");
		setBodyView(R.layout.dialog_newgame_online);
		setButtonTxt(R.id.ok, "Submit");

		AdapterItem[] list = new AdapterItem[]
			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
			new AdapterItem("Regular", Enums.REGULAR_CHESS),
			new AdapterItem("Any Type", Enums.ANY_CHESS) };

		ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		Spinner spinner = (Spinner) findViewById(R.id.game_type);
		spinner.setAdapter(adapter);

		// EventType dropdown
		list = new AdapterItem[]
			{new AdapterItem("Random", Enums.RANDOM),
			new AdapterItem("Invite", Enums.INVITE) };

		adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		spinner = (Spinner) findViewById(R.id.opp_type);
		spinner.setAdapter(adapter);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final Bundle data = new Bundle();

			final Spinner gametype = (Spinner) findViewById(R.id.game_type);
			final Spinner eventtype = (Spinner) findViewById(R.id.opp_type);

			data.putInt("gametype", ((AdapterItem) gametype.getSelectedItem()).id);
			data.putInt("opponent", ((AdapterItem) eventtype.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, data));
		}
		dismiss();
	}
}
