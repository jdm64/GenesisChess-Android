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

import java.util.Map.*;
import android.app.AlertDialog.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.RadioGroup.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import androidx.fragment.app.DialogFragment;

public class NewLocalGameDialog extends DialogFragment implements DialogInterface.OnClickListener, OnCheckedChangeListener
{
	public final static int MSG = 102;

	private Handler handle;
	private RadioGroup gametype_group;
	private RadioGroup white_group;
	private RadioGroup black_group;

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

		gametype_group = builder.getKey().findViewById(R.id.type_group);
		gametype_group.check(R.id.genesis_radio);

		white_group = builder.getKey().findViewById(R.id.white_group);
		white_group.check(R.id.white_human);
		white_group.setOnCheckedChangeListener(this);

		black_group = builder.getKey().findViewById(R.id.black_group);
		black_group.check(R.id.black_cpu);
		black_group.setOnCheckedChangeListener(this);

		return builder.getValue().create();
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int id)
	{
		if (radioGroup == white_group && id == R.id.white_cpu && black_group.getCheckedRadioButtonId() == R.id.black_cpu) {
			((RadioButton) black_group.findViewById(R.id.black_human)).toggle();
		} else if (radioGroup == black_group && id == R.id.black_cpu && white_group.getCheckedRadioButtonId() == R.id.white_cpu) {
			((RadioButton) white_group.findViewById(R.id.white_human)).toggle();
		}
	}

	public int getOpponent()
	{
		int isWhiteHuman = white_group.getCheckedRadioButtonId() == R.id.white_human ? 1 : 0;
		int isBlackHuman = black_group.getCheckedRadioButtonId() == R.id.black_human ? 2 : 0;

		switch (isWhiteHuman + isBlackHuman) {
		case 3:
			return Enums.HUMAN_OPPONENT;
		case 2:
			return Enums.CPU_WHITE_OPPONENT;
		case 1:
		default:
			return Enums.CPU_BLACK_OPPONENT;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			final Bundle data = new Bundle();
			final EditText text = getDialog().findViewById(R.id.game_name);

			int gameType = gametype_group.getCheckedRadioButtonId() == R.id.genesis_radio
				? Enums.GENESIS_CHESS : Enums.REGULAR_CHESS;

			data.putString("name", text.getText().toString().trim());
			data.putInt("gametype", gameType);
			data.putInt("opponent", getOpponent());

			handle.sendMessage(handle.obtainMessage(MSG, data));
		}
		dismiss();
	}
}
