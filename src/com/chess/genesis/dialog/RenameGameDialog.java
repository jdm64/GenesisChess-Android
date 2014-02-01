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

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.TextView.BufferType;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class RenameGameDialog extends BaseDialog
{
	public final static int MSG = 114;

	private final Handler handle;
	private final String gamename;
	private final int gameid;

	private EditText txtinput;

	public RenameGameDialog(final Context context, final Handler handler, final int id, final String name)
	{
		super(context);

		handle = handler;
		gameid = id;
		gamename = name;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Rename Game");
		setBodyView(R.layout.dialog_rename_game);
		setButtonTxt(R.id.ok, "Rename");

		txtinput = (EditText) findViewById(R.id.game_name_input);
		txtinput.setText(gamename, BufferType.EDITABLE);
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final GameDataDB db = new GameDataDB(v.getContext());
			db.renameLocalGame(gameid, txtinput.getText().toString().trim());
			db.close();
			handle.sendMessage(handle.obtainMessage(MSG));
		}
		dismiss();
	}
}
