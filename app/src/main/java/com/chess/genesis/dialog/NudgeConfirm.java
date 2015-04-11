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

import com.chess.genesis.*;

public class NudgeConfirm extends BaseDialog
{
	public final static int MSG = 116;

	private final Handler handle;
	private final String gameid;

	public NudgeConfirm(final Context context, final Handler handler)
	{
		this(context, handler, "");
	}

	public NudgeConfirm(final Context context, final Handler handler, final String GameID)
	{
		super(context);

		handle = handler;
		gameid = GameID;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Nudge Confirmation");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.ok, "Nudge");

		final TextView txt = (TextView) findViewById(R.id.text);
		txt.setText(R.string.nudge_confirm);
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG, gameid));
		dismiss();
	}
}
