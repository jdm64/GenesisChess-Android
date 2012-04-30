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

import android.app.*;
import android.content.*;
import android.os.*;

public class ProgressMsg extends ProgressDialog implements Runnable
{
	public final static int MSG = 121;

	private Handler handle = null;

	public ProgressMsg(final Context context)
	{
		super(context);
	}

	public ProgressMsg(final Context context, final Handler handler)
	{
		super(context);
		handle = handler;
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (handle != null)
			handle.sendMessage(handle.obtainMessage(MSG));
	}

	@Override
	public void onBackPressed()
	{
		// do nothing
	}

	public void remove()
	{
		new Thread(this).start();
	}

	public void setText(final String msg)
	{
		setMessage(msg);
		if (!isShowing())
			show();
	}

	@Override
	public synchronized void run()
	{
	try {
		Thread.sleep(256);
		dismiss();
	} catch (final InterruptedException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
