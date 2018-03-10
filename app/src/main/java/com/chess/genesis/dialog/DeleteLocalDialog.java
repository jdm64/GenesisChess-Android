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

import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class DeleteLocalDialog extends DialogFragment implements DialogInterface.OnClickListener
{
	public final static int MSG = 112;

	private Handler handle;
	private int gameid;
	private View view;

	public static DeleteLocalDialog create(Handler handler, int gameid)
	{
		DeleteLocalDialog dialog = new DeleteLocalDialog();
		dialog.handle = handler;
		dialog.gameid = gameid;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Activity activity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LayoutInflater inflater = activity.getLayoutInflater();

		view = inflater.inflate(R.layout.dialog_single_text, null);

		builder
				.setTitle("Delete Game")
				.setView(view)
				.setPositiveButton("Delete Game", this)
				.setNegativeButton("Cancel", this);

		TextView txt = view.findViewById(R.id.text);
		txt.setText(R.string.delete_local);

		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			try (GameDataDB db = new GameDataDB(getContext())) {
				db.deleteLocalGame(gameid);
			}
			handle.sendMessage(handle.obtainMessage(MSG));
		}
		dismiss();
	}
}
