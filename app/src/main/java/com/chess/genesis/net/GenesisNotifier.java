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

package com.chess.genesis.net;

import java.util.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import androidx.core.app.NotificationCompat.*;

public class GenesisNotifier extends Service implements Runnable
{
	public final static int DEFAULT_POLL_FREQ = 30;

	private final static int ERROR_NOTE = 1;
	public final static int YOURTURN_NOTE = 2;
	public final static int NEWMGS_NOTE = 4;

	private final static String YOURTURN_MSG = "It's your turn in a game you're in";
	private final static String NEWMSG_MSG = "A new message was posted to a game you're in";

	private NetworkClient net;
	private SocketClient socket;
	private GameDataDB db;
	private int lock;
	private boolean fromalarm;
	private boolean error;

	private final Handler handle = new Handler()
	{
		@Override
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;

		try {
			if (json.getString("result").equals("error")) {
				error = true;
				socket.disconnect();
			//	SendNotification(ERROR_NOTE, json.getString("reason"));
				return;
			}
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

			switch (msg.what) {
			case NetworkClient.SYNC_GAMES:
				NewMove(json);
				break;
			case NetworkClient.SYNC_MSGS:
				NewMsgs(json);
				break;
			case NetworkClient.GAME_STATUS:
				game_status(json);
				break;
			}
			// release lock
			lock--;
		}
	};

	public static void clearNotification(final Context context, final int id)
	{
		final GameDataDB db2 = new GameDataDB(context);
		final NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

		if ((id & YOURTURN_NOTE) != 0) {
			if (db2.getOnlineGameList(Enums.YOUR_TURN).getCount() == 0)
				nm.cancel(YOURTURN_NOTE);
		} else if ((id & NEWMGS_NOTE) != 0) {
			if (db2.getUnreadMsgCount() == 0)
				nm.cancel(NEWMGS_NOTE);
		}
		db2.close();
	}

	@Override
	public synchronized void run()
	{
		final Pref pref = new Pref(this);
		if (!pref.getBool(R.array.pf_isLoggedIn) || !pref.getBool(R.array.pf_noteEnabled)) {
			CancelWakup(this);
			stopSelf();
			return;
		} else if (internetIsActive() && fromalarm) {
			CheckServer();
		}
		ScheduleWakeup(this);
		stopSelf();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startid)
	{
		final Bundle bundle = intent.getExtras();
		fromalarm = bundle != null && bundle.getBoolean("fromAlarm", false);

		new Thread(this).start();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent)
	{
		return null;
	}

	public static void ScheduleWakeup(final Context context)
	{
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, Pref.getInt(context, R.array.pf_notifierPolling));
		final long start = cal.getTimeInMillis();
		final long interval = start - System.currentTimeMillis();

		final Intent intent = new Intent(context, GenesisAlarm.class);
		final PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC, start, interval, pintent);
	}

	private static void CancelWakup(final Context context)
	{
		final Intent intent = new Intent(context, GenesisAlarm.class);
		final PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pintent);
	}

	private boolean internetIsActive()
	{
		final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return (netInfo != null && netInfo.isConnected());
	}

	private void SendNotification(final int id, final String text)
	{
		final Intent intent = new Intent(this, GameListOnline.class);

		final PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification note = createNotification(id, text, pintent);

		nm.notify(id, note);
	}

	private Notification createNotification(final int id, final String text, final PendingIntent pintent)
	{
		final Builder noteBuilder = new Builder(getApplicationContext(), null);

		Bitmap icon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon), 96, 96, true);
		noteBuilder.setContentText(text).setSmallIcon(R.drawable.icon_note).setLargeIcon(icon);
		int color = R.color.blue_navy_400;

		switch (id) {
		case ERROR_NOTE:
			color = R.color.red_500;
			noteBuilder.setContentTitle("Error!");
			break;
		case YOURTURN_NOTE:
			// color already set
			noteBuilder.setContentTitle("It's Your turn");
			break;
		case NEWMGS_NOTE:
			color = R.color.green_light_A700;
			noteBuilder.setContentTitle("New Message");
			break;
		}

		noteBuilder.setColor(getResources().getColor(color));
		if (id == ERROR_NOTE) {
			noteBuilder.setAutoCancel(true);
			return noteBuilder.build();
		}
		noteBuilder.setOnlyAlertOnce(true).setContentIntent(pintent);

		return noteBuilder.build();
	}

	/*
	 * Sync Code
	 */

	private void trylock()
	{
	try {
		lock++;
		while (lock > 0 && !error)
			Thread.sleep(16);
		lock = 0;
	} catch (final InterruptedException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private void CheckServer()
	{
		error = false;
		lock = 0;
		socket = SocketClient.getNewInstance(this);
		net = new NetworkClient(socket, this, handle);
		db = new GameDataDB(this);

		final Pref pref = new Pref(this);
		final long mtime = pref.getLong(R.array.pf_lastmsgsync);
		final long gtime = pref.getLong(R.array.pf_lastgamesync);

		if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() > 0) {
			SendNotification(YOURTURN_NOTE, YOURTURN_MSG);
		} else {
			net.sync_games(gtime);
			net.run();
			trylock();

			if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() > 0)
				SendNotification(YOURTURN_NOTE, YOURTURN_MSG);
		}

		if (db.getUnreadMsgCount() > 0) {
			SendNotification(NEWMGS_NOTE, NEWMSG_MSG);
		} else {
			net.sync_msgs(mtime);
			net.run();
			trylock();

			if (db.getUnreadMsgCount() > 0)
				SendNotification(NEWMGS_NOTE, NEWMSG_MSG);
		}
		socket.disconnect();
		db.close();
	}

	private void NewMove(final JSONObject json)
	{
	try {
		final JSONArray ids = json.getJSONArray("gameids");

		for (int i = 0, len = ids.length(); i < len; i++) {
			if (error)
				return;
			net.game_status(ids.getString(i));
			net.run();

			lock++;
		}
		// Save sync time
		final PrefEdit pref = new PrefEdit(this);
		pref.putLong(R.array.pf_lastgamesync, json.getLong("time"));
		pref.commit();
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private void NewMsgs(final JSONObject json)
	{
	try {
		final JSONArray msgs = json.getJSONArray("msglist");
		final long time = json.getLong("time");

		for (int i = 0, len = msgs.length(); i < len; i++) {
			final JSONObject item = msgs.getJSONObject(i);
			db.insertMsg(item);
		}

		// Save sync time
		final PrefEdit pref = new PrefEdit(this);
		pref.putLong(R.array.pf_lastmsgsync, time);
		pref.commit();
	}  catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private void game_status(final JSONObject json)
	{
		db.updateOnlineGame(json);
	}
}
