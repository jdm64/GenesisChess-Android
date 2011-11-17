package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

class NewLocalGameDialog extends BaseDialog implements OnClickListener
{
	public final static int MSG = 102;

	private final Handler handle;

	private Spinner gametype_spin;
	private Spinner opponent_spin;

	public NewLocalGameDialog(final Context context, final Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("New Local Game");
		setBodyView(R.layout.dialog_newgame_local);
		setButtonTxt(R.id.ok, "Create Game");

		AdapterItem[] list = new AdapterItem[]
			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
			new AdapterItem("Regular", Enums.REGULAR_CHESS) };

		ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		gametype_spin = (Spinner) findViewById(R.id.game_type);
		gametype_spin.setAdapter(adapter);

		list = new AdapterItem[] {new AdapterItem("Computer As Black", Enums.CPU_BLACK_OPPONENT),
			new AdapterItem("Computer As White", Enums.CPU_WHITE_OPPONENT),
			new AdapterItem("Human", Enums.HUMAN_OPPONENT) };

		adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		opponent_spin = (Spinner) findViewById(R.id.opponent);
		opponent_spin.setAdapter(adapter);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final Bundle data = new Bundle();
			final EditText text = (EditText) findViewById(R.id.game_name);

			data.putString("name", text.getText().toString().trim());
			data.putInt("gametype", ((AdapterItem) gametype_spin.getSelectedItem()).id);
			data.putInt("opponent", ((AdapterItem) opponent_spin.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, data));
		}
		dismiss();
	}
}
