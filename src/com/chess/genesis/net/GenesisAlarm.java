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

package com.chess.genesis.net;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import java.util.*;

public class GenesisAlarm extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		if (intent.getAction() != null) {
			ScheduleWakeup(context);
			return;
		}

		final Intent nintent = new Intent(context, GenesisNotifier.class);
		final Bundle bundle = new Bundle();

		bundle.putBoolean("fromAlarm", true);
		nintent.putExtras(bundle);

		context.startService(nintent);
	}

	private void ScheduleWakeup(final Context context)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, pref.getInt("notifierPolling", GenesisNotifier.POLL_FREQ));

		final Intent intent = new Intent(context, GenesisAlarm.class);
		final PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
	}
}
