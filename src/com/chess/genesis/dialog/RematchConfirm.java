package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

class RematchConfirm extends Dialog implements OnClickListener
{
	public final static int MSG = 108;

	private final Handler handle;
	private final String opponent;

	public RematchConfirm(final Context context, final Handler handler, final String Opponent)
	{
		super(context);
		handle = handler;
		opponent = Opponent;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Rematch Confirmation");

		setContentView(R.layout.dialog_confirm_rematch);

		final TextView txt = (TextView) findViewById(R.id.rematch_confirm);
		txt.setText("Are you sure you want to invite " + opponent +
			" to a rematch game with the following settings?");

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(this);

		AdapterItem[] list = new AdapterItem[]
			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
			new AdapterItem("Regular", Enums.REGULAR_CHESS) };

		ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		Spinner spinner = (Spinner) findViewById(R.id.game_type);
		spinner.setAdapter(adapter);

		list = new AdapterItem[]
			{new AdapterItem("Random", Enums.RANDOM_OPP),
			new AdapterItem("White", Enums.WHITE_OPP),
			new AdapterItem("Black", Enums.BLACK_OPP) };

		adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		spinner = (Spinner) findViewById(R.id.color);
		spinner.setAdapter(adapter);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.ok:
			final Bundle data = new Bundle();

			Spinner spinner = (Spinner) findViewById(R.id.game_type);
			data.putInt("gametype", ((AdapterItem) spinner.getSelectedItem()).id);

			spinner = (Spinner) findViewById(R.id.color);
			data.putInt("color", ((AdapterItem) spinner.getSelectedItem()).id);

			data.putString("opp_name", opponent);

			handle.sendMessage(handle.obtainMessage(MSG, data));
			break;
		}
		dismiss();
	}
}
