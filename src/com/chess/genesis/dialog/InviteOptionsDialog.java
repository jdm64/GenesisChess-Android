package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

class InviteOptionsDialog extends BaseDialog implements OnClickListener
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
		super.onCreate(savedInstanceState);
		setTitle("Invite Game Options");
		setBodyView(R.layout.dialog_newgame_invite);
		setButtonTxt(R.id.ok, "Create Game");

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
		if (v.getId() == R.id.ok) {
			final EditText opp_name = (EditText) findViewById(R.id.opp_name);
			final Spinner color = (Spinner) findViewById(R.id.invite_color);

			settings.putString("opp_name", opp_name.getText().toString().trim());
			settings.putInt("color", ((AdapterItem) color.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, settings));
		}
		dismiss();
	}
}
