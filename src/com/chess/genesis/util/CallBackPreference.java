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

package com.chess.genesis.util;

import android.content.*;
import android.preference.*;
import android.util.*;

public class CallBackPreference extends DialogPreference
{
	public interface CallBack
	{
		void runCallBack(final CallBackPreference preference, final boolean result);
	}

	private CallBack action;
 
	public CallBackPreference(final Context context)
	{
		this(context, null);
	}

	public CallBackPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		setPersistent(false);
		action = null;
	}

	public void setCallBack(final CallBack callback)
	{
		action = callback;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		if (action == null)
			return;
		action.runCallBack(this, positiveResult);
	}
}
