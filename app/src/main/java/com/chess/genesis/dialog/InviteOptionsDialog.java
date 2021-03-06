/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.dialog;

import android.R.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.*;

public class InviteOptionsDialog extends BaseDialog
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

		final ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<>(getContext(), layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		final Spinner spinner = findViewById(R.id.invite_color);
		spinner.setAdapter(adapter);
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final EditText opp_name = findViewById(R.id.opp_name);
			final Spinner color = findViewById(R.id.invite_color);

			settings.putString("opp_name", opp_name.getText().toString().trim());
			settings.putInt("color", ((AdapterItem) color.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, settings));
		}
		dismiss();
	}
}
