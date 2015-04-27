/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.activity;

import android.content.*;
import android.support.v4.content.*;

public class BroadcastWrapper extends BroadcastReceiver
{
	private final Receiver rec;
	private IntentFilter filter;

	public interface Receiver
	{
		void onReceive(Intent intent);
		Context getActivity();
	}

	public BroadcastWrapper(Receiver receiver)
	{
		rec = receiver;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		rec.onReceive(intent);
	}

	public void setFilter(IntentFilter intentFilter)
	{
		filter = intentFilter;
	}

	public void register()
	{
		getLBM(rec.getActivity()).registerReceiver(this, filter);
	}

	public void unregister()
	{
		getLBM(rec.getActivity()).unregisterReceiver(this);
	}

	public static boolean send(Context context, Intent intent)
	{
		return getLBM(context).sendBroadcast(intent);
	}

	private static LocalBroadcastManager getLBM(Context context)
	{
		return LocalBroadcastManager.getInstance(context);
	}
}
