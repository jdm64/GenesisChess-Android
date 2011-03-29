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
import android.widget.Spinner;

class InviteOptionsDialog extends Dialog implements OnClickListener
{
	public final static int MSG = 104;

	private final Handler handle;
	private final Bundle settings;

	public InviteOptionsDialog(final Context context, final Handler handler, final Bundle _settings)
	{
		super(context);

		handle = handler;
		settings = _settings;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		setTitle("Invite Game Options");

		setContentView(R.layout.invite_options);

		Button button = (Button) findViewById(R.id.newgame_ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.newgame_cancel);
		button.setOnClickListener(this);

		// ColorType dropdown
		final AdapterItem[] list = new AdapterItem[]
			{new AdapterItem("Random", Enums.RANDOM_OPP),
			new AdapterItem("White", Enums.WHITE_OPP),
			new AdapterItem("Black", Enums.BLACK_OPP) };

		final ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this.getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		final Spinner spinner = (Spinner) findViewById(R.id.invite_color);
		spinner.setAdapter(adapter);
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.newgame_ok:
			final EditText opp_name = (EditText) findViewById(R.id.opp_name);
			final Spinner color = (Spinner) findViewById(R.id.invite_color);

			settings.putString("opp_name", opp_name.getText().toString());
			settings.putInt("color", ((AdapterItem) color.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, settings));
			break;
		}
		dismiss();
	}
}
