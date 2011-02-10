package com.chess.genesis;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class SyncGameList implements Runnable
{
	public final static int MSG = 101;

	private int lock = 0;
	private boolean error = false;

	private String username;

	private Handler callback;
	private NetworkClient net;

	private Context context;

	private Handler handle = new Handler()
	{
		public void handleMessage(Message msg)
		{
			JSONObject json = (JSONObject) msg.obj;

		try {
			if (json.getString("result").equals("error")) {
				callback.sendMessage(Message.obtain(callback, MSG, json));
				error = true;
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
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
			case NetworkClient.READ_INBOX:
				read_inbox(json);
				lock--;
				break;
			case NetworkClient.CLEAR_INBOX:
				lock--;
				break;
			}
		}
	};

	public SyncGameList(Context _context, Handler handler, String Username)
	{
		context = _context;
		callback = handler;
		username = Username;

		net = new NetworkClient(handle);
	}

	public void run()
	{
		net.read_inbox(username);
		net.run();
	try {
		lock = 1;
		while (lock > 0 && !error)
			Thread.sleep(16);
	} catch (java.lang.InterruptedException e) {
		e.printStackTrace();
	}

		if (!error)
			update_gamestatus();
	try {
		while (lock > 0 && !error)
			Thread.sleep(16);
	} catch (java.lang.InterruptedException e) {
		e.printStackTrace();
	}

		if (!error) {
			JSONObject json = new JSONObject();
			try {
				json.put("result", "ok");
				json.put("reason", "gamelist updated");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			callback.sendMessage(Message.obtain(callback, MSG, json));
		}
	}

	private void read_inbox(JSONObject json)
	{
	try {
		JSONArray games = json.getJSONArray("games");
		JSONArray msgs = json.getJSONArray("msgs");

		long mtime = 0;

		for (int i = 0; i < games.length(); i++) {
			JSONObject data = games.getJSONObject(i);

			long time = data.getLong("time");
			mtime = Math.max(time, mtime);

			String gameid = data.getString("gameid");

			if (error)
				return;
			net.game_info(username, gameid);
			net.run();

			lock++;
		}
		for (int i = 0; i < msgs.length(); i++) {
			JSONObject data = msgs.getJSONObject(i);

			long time = data.getLong("time");
			mtime = Math.max(time, mtime);

			String gameid = data.getString("gameid");
			String msg = data.getString("msg");
			String name = data.getString("username");

			GameDataDB db = new GameDataDB(context);
			db.insertMsg(gameid, time, username, msg);
			db.close();
		}
		if (error)
			return;
		net.clear_inbox(username, mtime);
		net.run();

		lock++;
	} catch (JSONException e) {
		e.printStackTrace();
	}
	}

	private void update_gamestatus()
	{
		GameDataDB db = new GameDataDB(context);
		ObjectArray<String> list = db.getOnlineGameIds();
		db.close();

		for (int i = 0; i < list.size(); i++) {
			if (error)
				return;
			net.game_status(username, list.get(i));
			net.run();

			lock++;
		}
	}

	private void game_info(JSONObject json)
	{
	try {
		String gameid = json.getString("gameid");
		String white = json.getString("white");
		String black = json.getString("black");
		long ctime = json.getLong("ctime");
		int gametype = Enums.GameType(json.getString("gametype"));

		GameDataDB db = new GameDataDB(context);
		db.insertOnlineGame(gameid, gametype, ctime, white, black);
		db.close();
	} catch (JSONException e) {
		e.printStackTrace();
	}
	}

	private void game_status(JSONObject json)
	{
	try {
		String gameid = json.getString("gameid");
		String zfen = json.getString("zfen");
		String history = json.getString("history");
		long stime = json.getLong("stime");

		GameDataDB db = new GameDataDB(context);
		db.updateOnlineGame(gameid, stime, zfen, history);
		db.close();
	} catch (JSONException e) {
		e.printStackTrace();
	}
	}
}