package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SyncGameList implements Runnable
{
	public final static int MSG = 101;

	private final Context context;
	private final Handler callback;
	private final NetworkClient net;

	private int lock = 0;
	private int sync_type = Enums.ONLINE_GAME;
	private boolean error = false;
	private boolean fullsync = false;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;

		try {
			if (json.getString("result").equals("error")) {
				callback.sendMessage(Message.obtain(callback, MSG, json));
				error = true;
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

			switch (msg.what) {
			case NetworkClient.GAME_STATUS:
				game_status(json);
				lock--;
				break;
			case NetworkClient.GAME_INFO:
				game_info(json);
				lock--;
				break;
			case NetworkClient.GAME_DATA:
				game_data(json);
				lock--;
				break;
			case NetworkClient.SYNC_GAMIDS:
				if (sync_type == Enums.ONLINE_GAME)
					sync_active(json);
				else
					sync_archive(json);
				lock--;
				break;
			case NetworkClient.SYNC_LIST:
				sync_recent(json);
				lock--;
				break;
			case NetworkClient.SYNC_MSGS:
				saveMsgs(json);
				lock--;
				break;
			}
		}
	};

	public SyncGameList(final Context _context, final Handler handler)
	{
		context = _context;
		callback = handler;

		net = new NetworkClient(context, handle);
	}

	private void trylock()
	{
	try {
		lock++;
		while (lock > 0 && !error)
			Thread.sleep(16);
		lock = 0;
	} catch (java.lang.InterruptedException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	public void setFullSync(final boolean value)
	{
		fullsync = value;
	}

	public void run()
	{
		if (fullsync) {
			sync_type = Enums.ONLINE_GAME;
			net.sync_gameids("active");
			net.run();

			trylock();

			sync_type = Enums.ARCHIVE_GAME;
			net.sync_gameids("archive");
			net.run();
			trylock();
		} else {
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			final long mtime = pref.getLong("lastmsgsync", 0);
			final long gtime = pref.getLong("lastgamesync", 0);

			net.sync_msgs(mtime);
			net.run();
			trylock();

			net.sync_list(gtime);
			net.run();
			trylock();
		}

		if (!error) {
			final JSONObject json = new JSONObject();
			try {
				json.put("result", "ok");
				json.put("reason", "gamelist updated");
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
			callback.sendMessage(Message.obtain(callback, MSG, json));
		}
	}

	private void sync_recent(final JSONObject json)
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
		Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
		pref.putLong("lastgamesync", time);
		pref.commit();
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	private void sync_active(final JSONObject json)
	{
		long time = 0;
		final GameDataDB db = new GameDataDB(context);
		final ObjectArray<String> list = db.getOnlineGameIds();
		db.close();

		final ArrayList<String> list_have = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++)
			list_have.add(list.get(i));

		final ArrayList<String> list_need = new ArrayList<String>();
	try {
		final JSONArray ids = json.getJSONArray("gameids");
		time = json.getLong("time");
		for (int i = 0; i < ids.length(); i++)
			list_need.add(ids.getString(i));
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}

		list_need.removeAll(list_have);

		for (int i = 0; i < list_need.size(); i++) {
			if (error)
				return;
			net.game_info(list_need.get(i));
			net.run();

			lock++;
		}
		// Save sync time
		Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
		pref.putLong("lastgamesync", time);
		pref.commit();
	}

	private void sync_archive(final JSONObject json)
	{
		final GameDataDB db = new GameDataDB(context);
		final ObjectArray<String> list = db.getArchiveGameIds();
		db.close();

		final ArrayList<String> list_have = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++)
			list_have.add(list.get(i));

		final ArrayList<String> list_need = new ArrayList<String>();
	try {
		final JSONArray ids = json.getJSONArray("gameids");
		for (int i = 0; i < ids.length(); i++)
			list_need.add(ids.getString(i));
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}

		list_need.removeAll(list_have);

		for (int i = 0; i < list_need.size(); i++) {
			if (error)
				return;
			net.game_data(list_need.get(i));
			net.run();

			lock++;
		}
	}

	private void game_info(final JSONObject json)
	{
		final GameDataDB db = new GameDataDB(context);
		db.insertOnlineGame(json);
		db.close();
	}

	private void game_status(final JSONObject json)
	{
		final GameDataDB db = new GameDataDB(context);
		db.updateOnlineGame(json);
		db.close();
	}

	private void game_data(final JSONObject json)
	{
		final GameDataDB db = new GameDataDB(context);
		db.insertArchiveGame(json);
		db.close();
	}

	private void saveMsgs(final JSONObject data)
	{
	try {
		final JSONArray msgs = data.getJSONArray("msglist");
		final long time = data.getLong("time");
		final GameDataDB db = new GameDataDB(context);

		for (int i = 0; i < msgs.length(); i++) {
			final JSONObject item = msgs.getJSONObject(i);
			db.insertMsg(item);
		}
		db.close();

		// Save sync time
		Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
		pref.putLong("lastmsgsync", time);
		pref.commit();
	}  catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
