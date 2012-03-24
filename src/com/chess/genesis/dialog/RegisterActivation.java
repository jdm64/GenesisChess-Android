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

package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

class RegisterActivation extends BaseDialog implements OnClickListener
{
	public final static int MSG = 103;

	private final Handler handle;

	public RegisterActivation(final Context context, final Handler handler)
	{
		super(context, BaseDialog.CANCEL);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Activate Account");
		setBodyView(R.layout.dialog_single_text);
		setButtonTxt(R.id.cancel, "Close");

		final RobotoText txt = (RobotoText) findViewById(R.id.text);
		txt.setText(R.string.register_activation);
	}

	public void onClick(final View v)
	{
		handle.sendMessage(handle.obtainMessage(MSG));

		dismiss();
	}
}
