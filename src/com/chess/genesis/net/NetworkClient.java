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

import android.content.*;
import android.os.*;
import android.preference.*;
import com.chess.genesis.data.*;
import java.io.*;
import java.net.*;
import org.json.*;

public class NetworkClient implements Runnable
{
	public final static int NONE = 0;
	public final static int LOGIN = 1;
	public final static int REGISTER = 2;
	public final static int JOIN_GAME = 3;
	public final static int NEW_GAME = 4;
	public final static int GAME_STATUS = 7;
	public final static int GAME_INFO = 8;
	public final static int SUBMIT_MOVE = 9;
	public final static int SUBMIT_MSG = 10;
	public final static int SYNC_GAMIDS = 11;
	public final static int GAME_SCORE = 12;
	public final static int GAME_DATA = 13;
	public final static int RESIGN_GAME = 14;
	public final static int SYNC_GAMES = 15;
	public final static int SYNC_MSGS = 16;
	public final static int GET_OPTION = 17;
	public final static int SET_OPTION = 18;
	public final static int POOL_INFO = 19;
	public final static int NUDGE_GAME = 20;
	public final static int IDLE_RESIGN = 21;
	public final static int USER_STATS = 22;
	public final static int GAME_DRAW = 23;

	private final static String CANT_CONTACT_MSG = "Can't contact server for sending data";
	private final static String LOST_CONNECTION_MSG = "Lost connection during sending data";
	private final static String SERVER_ILLOGICAL_MSG = "Server response illogical";

	private final Context context;
	private final Handler callback;
	private final SocketClient socket;

	private JSONObject request;
	private int fid = NONE;
	private boolean loginRequired;
	private boolean error = false;

	public NetworkClient(final Context _context, final Handler handler)
	{
		callback = handler;
		context = _context;
		socket = SocketClient.getInstance();
	}

	public NetworkClient(final SocketClient Socket, final Context _context, final Handler handler)
	{
		callback = handler;
		context = _context;
		socket = Socket;
	}

	// Crypto.LoginKey makes a network connection, but all
	// network calls must be on a non-main thread. This finishes
	// the json setup started in login_user.
	private void login_setup()
	{
		final JSONObject json = new JSONObject();

		try {
			try {
				request.put("passhash", Crypto.LoginKey(socket, request.getString("passhash")));
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} catch (final SocketException e) {
			try {
				json.put("result", "error");
				json.put("reason", CANT_CONTACT_MSG);
				socket.logError(context, e, request);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put("result", "error");
				json.put("reason", LOST_CONNECTION_MSG);
				socket.logError(context, e, request);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		}
		if (error)
			callback.sendMessage(Message.obtain(callback, fid, json));
	}

	private JSONObject send_request(final JSONObject data)
	{
		JSONObject json = new JSONObject();

		try {
			socket.write(data);
		} catch (final SocketException e) {
			try {
				json.put("result", "error");
				json.put("reason", CANT_CONTACT_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put("result", "error");
				json.put("reason", LOST_CONNECTION_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		}
		if (error)
			return json;

		try {
			json = socket.read();
		} catch (final SocketException e) {
			try {
				json.put("result", "error");
				json.put("reason", CANT_CONTACT_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put("result", "error");
				json.put("reason", LOST_CONNECTION_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final JSONException e) {
			try {
				json.put("result", "error");
				json.put("reason", SERVER_ILLOGICAL_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		}
		return json;
	}

	private boolean relogin()
	{
		if (socket.getIsLoggedIn())
			return true;

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final String username = pref.getString(PrefKey.USERNAME, PrefKey.KEYERROR);
		final String password = pref.getString(PrefKey.PASSHASH, PrefKey.KEYERROR);

		JSONObject json = new JSONObject();

		try {
			try {
				json.put("request", "login");
				json.put("username", username);
				json.put("passhash", Crypto.LoginKey(socket, password));
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} catch (final SocketException e) {
			try {
				json.put("result", "error");
				json.put("reason", CANT_CONTACT_MSG);
				socket.logError(context, e, json);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put("result", "error");
				json.put("reason", LOST_CONNECTION_MSG);
				socket.logError(context, e, request);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		}
		if (error) {
			callback.sendMessage(Message.obtain(callback, fid, json));
			return false;
		}

		// Send login request
		json = send_request(json);

		try {
			if (!json.getString("result").equals("ok")) {
				callback.sendMessage(Message.obtain(callback, fid, json));
				return false;
			}
			socket.setIsLoggedIn(true);
			return true;
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void run()
	{
		if (fid == LOGIN)
			login_setup();

		if (error || (loginRequired && !relogin())) {
			error = false;
			socket.disconnect();
			return;
		}

		final JSONObject json = send_request(request);
		if (error) {
			error = false;
			socket.disconnect();
		}

		callback.sendMessage(Message.obtain(callback, fid, json));
	}

	public void register(final String username, final String password, final String email)
	{
		fid = REGISTER;
		loginRequired = false;

		request = new JSONObject();

		try {
			request.put("request", "register");
			request.put("username", username);
			request.put("passhash", Crypto.HashPasswd(password));
			request.put("email", email);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void login_user(final String username, final String password)
	{
		fid = LOGIN;
		loginRequired = false;

		request = new JSONObject();

		try {
			request.put("request", "login");
			request.put("username", username);

			// temporarily save password to passhash
			// login_setup will finish the creation of
			// the json in a new thread.
			request.put("passhash", password);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void join_game(final String gametype)
	{
		fid = JOIN_GAME;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "joingame");
			request.put("gametype", gametype);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void new_game(final String opponent, final String gametype, final String color)
	{
		fid = NEW_GAME;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "newgame");
			request.put("opponent", opponent);
			request.put("gametype", gametype);
			request.put("color", color);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public void submit_move(final String gameid, final String move)
	{
		fid = SUBMIT_MOVE;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "sendmove");
			request.put("gameid", gameid);
			request.put("move", move);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void resign_game(final String gameid)
	{
		fid = RESIGN_GAME;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "resign");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void submit_msg(final String gameid, final String msg)
	{
		fid = SUBMIT_MSG;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "sendmsg");
			request.put("gameid", gameid);
			request.put("txt", msg);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void sync_msgs(final long time)
	{
		fid = SYNC_MSGS;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "syncmsgs");
			request.put("time", time);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void game_status(final String gameid)
	{
		fid = GAME_STATUS;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "gamestatus");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void game_info(final String gameid)
	{
		fid = GAME_INFO;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "gameinfo");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void game_data(final String gameid)
	{
		fid = GAME_DATA;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "gamedata");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void sync_gameids(final String type)
	{
		fid = SYNC_GAMIDS;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "gameids");
			request.put("type", type);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void sync_games(final long time)
	{
		fid = SYNC_GAMES;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "syncgames");
			request.put("time", time);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void game_score(final String gameid)
	{
		fid = GAME_SCORE;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "gamescore");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public <Type> void set_option(final String option, final Type value)
	{
		fid = SET_OPTION;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "setoption");
			request.put("option", option);
			request.put("value", value);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void get_option(final String option)
	{
		fid = GET_OPTION;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "getoption");
			request.put("option", option);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void pool_info()
	{
		fid = POOL_INFO;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "poolinfo");
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void nudge_game(final String gameid)
	{
		fid = NUDGE_GAME;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "nudge");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void idle_resign(final String gameid)
	{
		fid = IDLE_RESIGN;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "idleresign");
			request.put("gameid", gameid);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void user_stats(final String username)
	{
		fid = USER_STATS;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "userstats");
			request.put("username", username);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void game_draw(final String gameid, final String action)
	{
		fid = GAME_DRAW;
		loginRequired = true;

		request = new JSONObject();

		try {
			request.put("request", "draw");
			request.put("gameid", gameid);
			request.put("action", action);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
