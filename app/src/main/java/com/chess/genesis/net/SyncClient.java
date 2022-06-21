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
import java.util.concurrent.*;
import android.content.*;
import android.os.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class SyncClient implements Runnable, Handler.Callback
{
	public final static int MSG = 101;

	public final static int FULL_SYNC = 0;
	private final static int REGULAR_SYNC = 1;
	public final static int ACTIVE_SYNC = 2;
	public final static int ARCHIVE_SYNC = 3;
	public final static int MSG_SYNC = 4;

	private final Context context;
	private final Handler callback;
	private final Handler handle = new Handler(this);
	private final NetworkClient net;

	private int lock = 0;
	private int syncType;
	private int gameType = Enums.ONLINE_GAME;
	private boolean error = false;

	@Override
	public boolean handleMessage(final Message msg)
	{
		final JSONObject json = (JSONObject) msg.obj;

	try {
		if (json.getString("result").equals("error")) {
			callback.sendMessage(Message.obtain(callback, MSG, json));
			error = true;
			return true;
		}
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}

		switch (msg.what) {
		case NetworkClient.GAME_STATUS:
			game_status(json);
			break;
		case NetworkClient.GAME_INFO:
			game_info(json);
			break;
		case NetworkClient.GAME_DATA:
			game_data(json);
			break;
		case NetworkClient.SYNC_GAMIDS:
			if (gameType == Enums.ONLINE_GAME)
				sync_active(json);
			else
				sync_archive(json);
			break;
		case NetworkClient.SYNC_GAMES:
			sync_recent(json);
			break;
		case NetworkClient.SYNC_MSGS:
			saveMsgs(json);
			break;
		}
		// release lock
		lock--;
		return true;
	}

	public SyncClient(final Context _context, final Handler handler)
	{
		context = _context;
		callback = handler;

		syncType = REGULAR_SYNC;
		net = new NetworkClient(context, handle);
	}

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

	public void setSyncType(final int Type)
	{
		syncType = Type;
	}

	@Override
	public synchronized void run()
	{
		switch (syncType) {
		case FULL_SYNC:
			gameType = Enums.ONLINE_GAME;
			net.sync_gameids("active");
			net.run();
			trylock();

			gameType = Enums.ARCHIVE_GAME;
			net.sync_gameids("archive");
			net.run();
			trylock();

			net.sync_msgs(0);
			net.run();
			trylock();
			break;
		case REGULAR_SYNC:
			final Pref pref = new Pref(context);
			final long mtime = pref.getLong(R.array.pf_lastmsgsync);
			final long gtime = pref.getLong(R.array.pf_lastgamesync);

			net.sync_games(gtime);
			net.run();
			trylock();

			net.sync_msgs(mtime);
			net.run();
			trylock();
			break;
		case ACTIVE_SYNC:
			gameType = Enums.ONLINE_GAME;
			net.sync_gameids("active");
			net.run();
			trylock();
			break;
		case ARCHIVE_SYNC:
			gameType = Enums.ARCHIVE_GAME;
			net.sync_gameids("archive");
			net.run();
			trylock();
			break;
		case MSG_SYNC:
			net.sync_msgs(0);
			net.run();
			trylock();
			break;
		}

		if (!error) {
			final JSONObject json = new JSONObject();
			try {
				json.put("result", "ok");
				json.put("reason", "gamelist updated");
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			callback.sendMessage(Message.obtain(callback, MSG, json));
		}
	}

	private void sync_recent(final JSONObject json)
	{
	try {
		final JSONArray ids = json.getJSONArray("gameids");
		final ExecutorService pool = Executors.newCachedThreadPool();

		for (int i = 0, len = ids.length(); i < len; i++) {
			if (error)
				return;
			final NetworkClient nc = new NetworkClient(context, handle);
			nc.game_status(ids.getString(i));
			pool.submit(nc);

			lock++;
		}
		// Save sync time
		new PrefEdit(context)
			.putLong(R.array.pf_lastgamesync, json.getLong("time"))
			.commit();
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private void sync_active(final JSONObject json)
	{
	try {
		final ArrayList<String> list_need = getNeedList(json.getJSONArray("gameids"));
		final ExecutorService pool = Executors.newCachedThreadPool();

		for (final String item : list_need) {
			if (error)
				return;
			final NetworkClient nc = new NetworkClient(context, handle);
			nc.game_info(item);
			pool.submit(nc);

			lock++;
		}
		// don't save time if only syncing active
		if (syncType == ACTIVE_SYNC)
			return;
		// Save sync time
		final long time = json.getLong("time");
		new PrefEdit(context)
			.putLong(R.array.pf_lastgamesync, time)
			.commit();
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private void sync_archive(final JSONObject json)
	{
	try {
		final ArrayList<String> list_need = getNeedList(json.getJSONArray("gameids"));
		final ExecutorService pool = Executors.newCachedThreadPool();

		for (final String item : list_need) {
			if (error)
				return;
			final NetworkClient nc = new NetworkClient(context, handle);
			nc.game_data(item);
			pool.submit(nc);

			lock++;
		}
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private ArrayList<String> getNeedList(final JSONArray ids)
	{
	try {
		final ArrayList<String> list_need = new ArrayList<>(ids.length());
		for (int i = 0, len = ids.length(); i < len; i++)
			list_need.add(ids.getString(i));

		if (syncType == ACTIVE_SYNC || syncType == ARCHIVE_SYNC)
			return list_need;

		final GameDataDB db = new GameDataDB(context);
		final List<String> list = gameType == Enums.ONLINE_GAME?
			db.getOnlineGameIds() : db.getArchiveGameIds();
		db.close();

		final ArrayList<String> list_have = new ArrayList<>(list.size());
		list_have.addAll(list);

		list_need.removeAll(list_have);
		return list_need;
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
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

		for (int i = 0, len = msgs.length(); i < len; i++) {
			final JSONObject item = msgs.getJSONObject(i);
			db.insertMsg(item);
		}
		db.close();

		// Save sync time
		new PrefEdit(context)
			.putLong(R.array.pf_lastmsgsync, time)
			.commit();
	}  catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}
}
