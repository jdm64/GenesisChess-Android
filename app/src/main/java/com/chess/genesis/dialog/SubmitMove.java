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

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import com.chess.genesis.*;

public class SubmitMove extends Dialog implements OnClickListener
{
	public final static int MSG = 122;

	private final Handler handle;

	public SubmitMove()
	{
		super(null, R.style.BlankDialog);
		handle = null;
	}

	public SubmitMove(final Context context, final Handler handler)
	{
		super(context, R.style.BlankDialog);

		handle = handler;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog_submitmove_phone);
		getWindow().setGravity(Gravity.BOTTOM);

		View image = findViewById(R.id.submit);
		image.setOnClickListener(this);
		image = findViewById(R.id.cancel);
		image.setOnClickListener(this);
	}

	@Override
	public void onBackPressed()
	{
		handle.sendMessage(handle.obtainMessage(MSG, false));
		dismiss();
	}

	@Override
	public void onClick(final View v)
	{
		handle.sendMessage(handle.obtainMessage(MSG, v.getId() == R.id.submit));
		dismiss();
	}
}
