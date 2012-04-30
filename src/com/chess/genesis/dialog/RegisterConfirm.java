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
import android.widget.*;
import com.chess.genesis.*;

public class RegisterConfirm extends BaseDialog
{
	public final static int MSG = 106;

	private final Handler handle;
	private final Bundle data;

	public RegisterConfirm(final Context context, final Handler handler, final Bundle bundle)
	{
		super(context);

		handle = handler;
		data = bundle;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Register Confirmation");
		setBodyView(R.layout.dialog_confirm_register);
		setButtonTxt(R.id.ok, "Register");

		TextView text = (TextView) findViewById(R.id.username);
		text.setText(data.getString("username"));

		text = (TextView) findViewById(R.id.email);
		text.setText(data.getString("email"));
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			handle.sendMessage(handle.obtainMessage(MSG, data));
		dismiss();
	}
}
