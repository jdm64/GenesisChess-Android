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

public class NewOnlineGameDialog extends BaseDialog implements OnClickListener
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

	@Override
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
