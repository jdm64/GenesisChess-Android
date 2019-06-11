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
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import androidx.fragment.app.DialogFragment;

public class DeleteLocalDialog extends DialogFragment implements DialogInterface.OnClickListener
{
	public final static int MSG = 112;

	private Handler handle;
	private int gameid;

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
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_single_text);

		builder.getValue()
				.setTitle("Delete Game")
				.setPositiveButton("Delete Game", this)
				.setNegativeButton("Cancel", this);

		TextView txt = builder.getKey().findViewById(R.id.text);
		txt.setText(R.string.delete_local);

		return builder.getValue().create();
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
