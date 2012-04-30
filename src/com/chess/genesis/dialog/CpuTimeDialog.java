
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
import com.chess.genesis.*;
import com.chess.genesis.view.*;

public class CpuTimeDialog extends BaseDialog
{
	public final static int MSG = 110;

	private final Handler handle;
	private final int time;

	public CpuTimeDialog(final Context context, final Handler handler, final int Time)
	{
		super(context);

		handle = handler;
		time = Time;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Set CPU Time Limit");
		setBodyView(R.layout.dialog_cputime);
		setButtonTxt(R.id.ok, "Set Time");

		final NumberSpinner number = (NumberSpinner) findViewById(R.id.time);
		number.setRange(1, 30);
		number.setCurrent(time);
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final NumberSpinner number = (NumberSpinner) findViewById(R.id.time);
			final Integer value = Integer.valueOf(number.getCurrent());
			handle.sendMessage(handle.obtainMessage(MSG, value));
		}
		dismiss();
	}
}
