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
import android.content.DialogInterface.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import androidx.fragment.app.DialogFragment;

public class RegisterConfirm extends DialogFragment implements OnClickListener
{
	public final static int MSG = 106;

	private Handler handle;
	private Bundle data;

	public static RegisterConfirm create(Handler handler, Bundle bundle)
	{
		RegisterConfirm dialog = new RegisterConfirm();
		dialog.handle = handler;
		dialog.data = bundle;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_confirm_register);

		builder.getValue()
		    .setTitle("Register Confirmation")
		    .setPositiveButton("Register", this)
		    .setNegativeButton("Cancel", this);

		View view = builder.getKey();
		TextView text = view.findViewById(R.id.username);
		text.setText(data.getString("username"));

		text = view.findViewById(R.id.email);
		text.setText(data.getString("email"));

		return builder.getValue().create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			handle.sendMessage(handle.obtainMessage(MSG, data));
		}
		dismiss();
	}
}
