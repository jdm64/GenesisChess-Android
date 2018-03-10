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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.*;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.*;
import android.widget.TextView.BufferType;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class RenameGameDialog extends DialogFragment implements DialogInterface.OnClickListener
{
	public final static int MSG = 114;

	private Handler handle;
	private String gamename;
	private int gameid;
	private View view;
	private EditText txtinput;

	public static RenameGameDialog create(Handler handler, int id, String name)
	{
		RenameGameDialog dialog = new RenameGameDialog();
		dialog.handle = handler;
		dialog.gameid = id;
		dialog.gamename = name;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Activity activity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LayoutInflater inflater = activity.getLayoutInflater();

		view = inflater.inflate(R.layout.dialog_rename_game, null);

		builder
			.setTitle("Rename Game")
			.setView(view)
			.setPositiveButton("Rename", this)
			.setNegativeButton("Cancel", this);

		txtinput = view.findViewById(R.id.game_name_input);
		txtinput.setText(gamename, BufferType.EDITABLE);

		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			try (GameDataDB db = new GameDataDB(getContext())) {
				db.renameLocalGame(gameid, txtinput.getText().toString().trim());
			}
			handle.sendMessage(handle.obtainMessage(MSG));
		}
		dismiss();
	}
}
