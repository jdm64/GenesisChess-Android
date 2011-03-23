package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

class NewOnlineGameDialog extends Dialog implements OnClickListener
{
	public final static int MSG = 100;

	private Handler handle;

	public NewOnlineGameDialog(Context context, Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setTitle("New Online Game");

		setContentView(R.layout.newnetworkgame);

		Button button = (Button) findViewById(R.id.newgame_ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.newgame_cancel);
		button.setOnClickListener(this);

		AdapterItem[] list = new AdapterItem[]
			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
			 new AdapterItem("Regular", Enums.REGULAR_CHESS) };

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

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.newgame_ok:
			Bundle data = new Bundle();

			Spinner gametype = (Spinner) findViewById(R.id.game_type);
			Spinner eventtype = (Spinner) findViewById(R.id.opp_type);

			data.putInt("gametype", ((AdapterItem) gametype.getSelectedItem()).id);
			data.putInt("opponent", ((AdapterItem) eventtype.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, data));
		}
		dismiss();
	}
}
