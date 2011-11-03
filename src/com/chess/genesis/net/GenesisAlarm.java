package com.chess.genesis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import java.util.Calendar;

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

		final AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
	}
}
