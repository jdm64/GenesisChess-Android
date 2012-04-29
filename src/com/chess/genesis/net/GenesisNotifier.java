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
import android.content.SharedPreferences.Editor;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.NotificationCompat.Builder;
import com.chess.genesis.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import java.util.*;
import org.json.*;

public class GenesisNotifier extends Service implements Runnable
{
	public final static int POLL_FREQ = 30;

	public final static int ERROR_NOTE = 1;
	public final static int YOURTURN_NOTE = 2;
	public final static int NEWMGS_NOTE = 4;

	private NetworkClient net;
	private SocketClient socket;
	private GameDataDB db;
	private SharedPreferences pref;
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
			e.printStackTrace();
			throw new RuntimeException();
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
		pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (!pref.getBoolean("isLoggedIn", false) || !pref.getBoolean("noteEnabled", true)) {
			stopSelf();
			return;
		} else if (internetIsActive() && fromalarm) {
			CheckServer();
		}
		ScheduleWakeup();
		stopSelf();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startid)
	{
		Bundle bundle = null;

		if (intent == null)
			fromalarm = false;
		else if ((bundle = intent.getExtras()) == null)
			fromalarm = false;
		else
			fromalarm = bundle.getBoolean("fromAlarm", false);

		(new Thread(this)).start();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent)
	{
		return null;
	}

	private void ScheduleWakeup()
	{
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, pref.getInt("notifierPolling", GenesisNotifier.POLL_FREQ));

		final Intent intent = new Intent(this, GenesisAlarm.class);
		final PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
	}

	private boolean internetIsActive()
	{
		final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return (netInfo != null && netInfo.isConnected());
	}

	private void SendNotification(final int id, final String text)
	{
		final Intent intent;
		if (pref.getBoolean("tabletMode", false)) {
			intent = new Intent(this, MainMenuTablet.class);

			final Bundle bundle = new Bundle();
			bundle.putInt("loadFrag", Enums.ONLINE_LIST);
			intent.putExtras(bundle);
		} else {
			intent = new Intent(this, GameListOnline.class);
		}

		final PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification note = createNotification(id, text, pintent);

		nm.notify(id, note);
	}

	private Notification createNotification(final int id, final String text, final PendingIntent pintent)
	{
		final Builder noteBuilder = new Builder(getApplicationContext());

		noteBuilder.setContentText(text).setSmallIcon(R.drawable.icon);

		switch (id) {
		case ERROR_NOTE:
			noteBuilder.setContentTitle("Error!");
			break;
		case YOURTURN_NOTE:
			noteBuilder.setContentTitle("It's Your turn");
			break;
		case NEWMGS_NOTE:
			noteBuilder.setContentTitle("New Message");
			break;
		}

		if (id == ERROR_NOTE) {
			noteBuilder.setAutoCancel(true);
			return noteBuilder.getNotification();
		}
		noteBuilder.setOnlyAlertOnce(true).setContentIntent(pintent);

		if (pref.getBoolean("noteRingtoneEnable", false))
			noteBuilder.setSound(Uri.parse(pref.getString("noteRingtone", "content://settings/system/notification_sound")));
		if (pref.getBoolean("noteVibrateEnable", true))
			noteBuilder.setVibrate(parseVibrate());

		return noteBuilder.getNotification();
	}

	private long[] parseVibrate()
	{
		final String str = pref.getString("noteVibrate", "0,150");
		final String[] arr = str.trim().split(",");
		final long[] vib = new long[arr.length];

		for (int i = 0; i < arr.length; i++)
			vib[i] = Long.valueOf(arr[i]);
		return vib;
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
	} catch (final java.lang.InterruptedException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	public void CheckServer()
	{
		error = false;
		lock = 0;
		socket = SocketClient.getInstance(0);
		net = new NetworkClient(socket, this, handle);
		db = new GameDataDB(this);

		final long mtime = pref.getLong("lastmsgsync", 0);
		final long gtime = pref.getLong("lastgamesync", 0);

		if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() > 0) {
			SendNotification(YOURTURN_NOTE, "It's your turn in a game you're in");
		} else {
			net.sync_games(gtime);
			net.run();
			trylock();

			if (db.getOnlineGameList(Enums.YOUR_TURN).getCount() > 0)
				SendNotification(YOURTURN_NOTE, "It's your turn in a game you're in");
		}

		if (db.getUnreadMsgCount() > 0) {
			SendNotification(NEWMGS_NOTE, "A new message was posted to a game you're in");
		} else {
			net.sync_msgs(mtime);
			net.run();
			trylock();

			if (db.getUnreadMsgCount() > 0)
				SendNotification(NEWMGS_NOTE, "A new message was posted to a game you're in");
		}
		socket.disconnect();
		db.close();
	}

	private void NewMove(final JSONObject json)
	{
	try {
		final JSONArray ids = json.getJSONArray("gameids");
		final long time = json.getLong("time");

		for (int i = 0; i < ids.length(); i++) {
			if (error)
				return;
			net.game_status(ids.getString(i));
			net.run();

			lock++;
		}
		// Save sync time
		final Editor editor = pref.edit();
		editor.putLong("lastgamesync", time);
		editor.commit();
	} catch (final JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	private void NewMsgs(final JSONObject json)
	{
	try {
		final JSONArray msgs = json.getJSONArray("msglist");
		final long time = json.getLong("time");

		for (int i = 0; i < msgs.length(); i++) {
			final JSONObject item = msgs.getJSONObject(i);
			db.insertMsg(item);
		}

		// Save sync time
		final Editor editor = pref.edit();
		editor.putLong("lastmsgsync", time);
		editor.commit();
	}  catch (final JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	private void game_status(final JSONObject json)
	{
		db.updateOnlineGame(json);
	}
}
