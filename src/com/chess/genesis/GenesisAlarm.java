package com.chess.genesis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GenesisAlarm extends BroadcastReceiver
{
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		final Intent nintent = new Intent(context, GenesisNotifier.class);
		final Bundle bundle = new Bundle();

		bundle.putBoolean("fromAlarm", true);
		nintent.putExtras(bundle);

		context.startService(nintent);
	}
}
