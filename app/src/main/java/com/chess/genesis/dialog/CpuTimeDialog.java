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
import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.os.*;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.*;

import com.chess.genesis.*;

public class CpuTimeDialog extends DialogFragment implements OnClickListener
{
	public final static int MSG = 110;

	private Handler handle;
	private int time;

	public static CpuTimeDialog create(Handler handler, int Time)
	{
		CpuTimeDialog dialog = new CpuTimeDialog();
		dialog.handle = handler;
		dialog.time = Time;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_cputime);

		builder.getValue()
		    .setTitle("Set CPU Time Limit")
		    .setPositiveButton("Set Time", this)
		    .setNegativeButton("Cancel", this);

		NumberPicker number = builder.getKey().findViewById(R.id.time);
		number.setMinValue(1);
		number.setMaxValue(30);
		number.setValue(time);

		return builder.getValue().create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			NumberPicker number = getDialog().findViewById(R.id.time);
			Integer value = number.getValue();
			handle.sendMessage(handle.obtainMessage(MSG, value));
		}
		dismiss();
	}
}
