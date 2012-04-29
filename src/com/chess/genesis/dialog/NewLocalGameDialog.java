/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.dialog;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.*;

public class NewLocalGameDialog extends BaseDialog implements OnClickListener
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

	@Override
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
