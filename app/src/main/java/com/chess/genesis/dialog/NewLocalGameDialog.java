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

import android.app.AlertDialog.*;
import android.app.Dialog;
import android.content.*;
import android.os.*;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.*;
import java.util.Map.*;

public class NewLocalGameDialog extends DialogFragment implements DialogInterface.OnClickListener
{
	public final static int MSG = 102;

	private Handler handle;
	private Spinner gametype_spin;
	private Spinner opponent_spin;

	public static NewLocalGameDialog create(Handler handler)
	{
		NewLocalGameDialog dialog = new NewLocalGameDialog();
		dialog.handle = handler;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_newgame_local);

		builder.getValue()
			.setTitle("New Local Game")
			.setPositiveButton("Create Game", this)
			.setNegativeButton("Cancel", this);

		AdapterItem[] list = new AdapterItem[]
			{new AdapterItem("Genesis", Enums.GENESIS_CHESS),
			new AdapterItem("Regular", Enums.REGULAR_CHESS) };

		ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<>(builder.getKey().getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		gametype_spin = builder.getKey().findViewById(R.id.game_type);
		gametype_spin.setAdapter(adapter);

		list = new AdapterItem[] {new AdapterItem("CPU As Black", Enums.CPU_BLACK_OPPONENT),
			new AdapterItem("CPU As White", Enums.CPU_WHITE_OPPONENT),
			new AdapterItem("Human", Enums.HUMAN_OPPONENT) };

		adapter = new ArrayAdapter<>(builder.getKey().getContext(), android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);

		opponent_spin = builder.getKey().findViewById(R.id.opponent);
		opponent_spin.setAdapter(adapter);

		return builder.getValue().create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			final Bundle data = new Bundle();
			final EditText text = getDialog().findViewById(R.id.game_name);

			data.putString("name", text.getText().toString().trim());
			data.putInt("gametype", ((AdapterItem) gametype_spin.getSelectedItem()).id);
			data.putInt("opponent", ((AdapterItem) opponent_spin.getSelectedItem()).id);

			handle.sendMessage(handle.obtainMessage(MSG, data));
		}
		dismiss();
	}
}
