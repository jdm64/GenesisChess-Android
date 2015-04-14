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

import android.content.*;
import android.os.*;
import com.chess.genesis.*;
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

	private final static String _ACTION = "action";
	private final static String _COLOR = "color";
	private final static String _DRAW = "draw";
	private final static String _EMAIL = "email";
	private final static String _ERROR = "error";
	private final static String _GAME_DATA = "gamedata";
	private final static String _GAMEID = "gameid";
	private final static String _GAMEIDS = "gameids";
	private final static String _GAME_INFO = "gameinfo";
	private final static String _GAME_SCORE = "gamescore";
	private final static String _GAME_STATUS = "gamestatus";
	private final static String _GAME_TYPE = "gametype";
	private final static String _GET_OPTION = "getoption";
	private final static String _IDLE_RESIGN = "idleresign";
	private final static String _JOIN_GAME = "joingame";
	private final static String _LOGIN = "login";
	private final static String _MOVE = "move";
	private final static String _NEW_GAME = "newgame";
	private final static String _NUDGE = "nudge";
	private final static String _OK = "ok";
	private final static String _OPPONENT = "opponent";
	private final static String _OPTION = "option";
	private final static String _PASSHASH = "passhash";
	private final static String _POOL_INFO = "poolinfo";
	private final static String _REASON = "reason";
	private final static String _REGISTER = "register";
	private final static String _REQUEST = "request";
	private final static String _RESIGN = "resign";
	private final static String _RESULT = "result";
	private final static String _SEND_MOVE = "sendmove";
	private final static String _SEND_MSG = "sendmsg";
	private final static String _SET_OPTION = "setoption";
	private final static String _SYNC_GAMES = "syncgames";
	private final static String _SYNC_MSGS = "syncmsgs";
	private final static String _TIME = "time";
	private final static String _TXT = "txt";
	private final static String _TYPE = "type";
	private final static String _USERNAME = "username";
	private final static String _USER_STATS = "userstats";
	private final static String _VALUE = "value";

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
		socket = SocketClient.getInstance(context);
	}

	public NetworkClient(final SocketClient Socket, final Context _context, final Handler handler)
	{
		callback = handler;
		context = _context;
		socket = Socket;
	}

	private void newRequest(final String type, final int callbackId, final boolean requiresLogin)
	{
		fid = callbackId;
		loginRequired = requiresLogin;
		request = new JSONObject();

		try {
			request.put(_REQUEST, type);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private <Type> void addValue(final String key, final Type value)
	{
		try {
			request.put(key, value);
		} catch (final JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// Crypto.LoginKey makes a network connection, but all
	// network calls must be on a non-main thread. This finishes
	// the json setup started in login_user.
	private void login_setup()
	{
		final JSONObject json = new JSONObject();

		try {
			try {
				request.put(_PASSHASH, Crypto.LoginKey(socket, request.getString(_PASSHASH)));
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} catch (final SocketException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, CANT_CONTACT_MSG);
				socket.logError(context, e, request);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, LOST_CONNECTION_MSG);
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
				json.put(_RESULT, _ERROR);
				json.put(_REASON, CANT_CONTACT_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, LOST_CONNECTION_MSG);
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
				json.put(_RESULT, _ERROR);
				json.put(_REASON, CANT_CONTACT_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, LOST_CONNECTION_MSG);
				socket.logError(context, e, data);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final JSONException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, SERVER_ILLOGICAL_MSG);
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

		final Pref pref = new Pref(context);
		final String username = pref.getString(R.array.pf_username);
		final String password = pref.getString(R.array.pf_passhash);

		JSONObject json = new JSONObject();

		try {
			try {
				json.put(_REQUEST, _LOGIN);
				json.put(_USERNAME, username);
				json.put(_PASSHASH, Crypto.LoginKey(socket, password));
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} catch (final SocketException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, CANT_CONTACT_MSG);
				socket.logError(context, e, json);
			} catch (final JSONException j) {
				throw new RuntimeException(j.getMessage(), j);
			}
			error = true;
		} catch (final IOException e) {
			try {
				json.put(_RESULT, _ERROR);
				json.put(_REASON, LOST_CONNECTION_MSG);
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
			if (!json.getString(_RESULT).equals(_OK)) {
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
		newRequest(_REGISTER, REGISTER, false);
		addValue(_USERNAME, username);
		addValue(_PASSHASH, Crypto.HashPasswd(password));
		addValue(_EMAIL, email);
	}

	public void login_user(final String username, final String password)
	{
		newRequest(_LOGIN, LOGIN, false);
		addValue(_USERNAME, username);

		/*
		 * temporarily save password to passhash login_setup will finish
		 * the creation of the json in a new thread.
		 */
		addValue(_PASSHASH, password);
	}

	public void join_game(final String gametype)
	{
		newRequest(_JOIN_GAME, JOIN_GAME, true);
		addValue(_GAME_TYPE, gametype);
	}

	public void new_game(final String opponent, final String gametype, final String color)
	{
		newRequest(_NEW_GAME, NEW_GAME, true);
		addValue(_OPPONENT, opponent);
		addValue(_GAME_TYPE, gametype);
		addValue(_COLOR, color);
	}

	public void submit_move(final String gameid, final String move)
	{
		newRequest(_SEND_MOVE, SUBMIT_MOVE, true);
		addValue(_GAMEID, gameid);
		addValue(_MOVE, move);
	}

	public void resign_game(final String gameid)
	{
		newRequest(_RESIGN, RESIGN_GAME, true);
		addValue(_GAMEID, gameid);
	}

	public void submit_msg(final String gameid, final String msg)
	{
		newRequest(_SEND_MSG, SUBMIT_MSG, true);
		addValue(_GAMEID, gameid);
		addValue(_TXT, msg);
	}

	public void sync_msgs(final long time)
	{
		newRequest(_SYNC_MSGS, SYNC_MSGS, true);
		addValue(_TIME, time);
	}

	public void game_status(final String gameid)
	{
		newRequest(_GAME_STATUS, GAME_STATUS, true);
		addValue(_GAMEID, gameid);
	}

	public void game_info(final String gameid)
	{
		newRequest(_GAME_INFO, GAME_INFO, true);
		addValue(_GAMEID, gameid);
	}

	public void game_data(final String gameid)
	{
		newRequest(_GAME_DATA, GAME_DATA, true);
		addValue(_GAMEID, gameid);
	}

	public void sync_gameids(final String type)
	{
		newRequest(_GAMEIDS, SYNC_GAMIDS, true);
		addValue(_TYPE, type);
	}

	public void sync_games(final long time)
	{
		newRequest(_SYNC_GAMES, SYNC_GAMES, true);
		addValue(_TIME, time);
	}

	public void game_score(final String gameid)
	{
		newRequest(_GAME_SCORE, GAME_SCORE, true);
		addValue(_GAMEID, gameid);
	}

	public <Type> void set_option(final String option, final Type value)
	{
		newRequest(_SET_OPTION, SET_OPTION, true);
		addValue(_OPTION, option);
		addValue(_VALUE, value);
	}

	public void get_option(final String option)
	{
		newRequest(_GET_OPTION, GET_OPTION, true);
		addValue(_OPTION, option);
	}

	public void pool_info()
	{
		newRequest(_POOL_INFO, POOL_INFO, true);
	}

	public void nudge_game(final String gameid)
	{
		newRequest(_NUDGE, NUDGE_GAME, true);
		addValue(_GAMEID, gameid);
	}

	public void idle_resign(final String gameid)
	{
		newRequest(_IDLE_RESIGN, IDLE_RESIGN, true);
		addValue(_GAMEID, gameid);
	}

	public void user_stats(final String username)
	{
		newRequest(_USER_STATS, USER_STATS, true);
		addValue(_USERNAME, username);
	}

	public void game_draw(final String gameid, final String action)
	{
		newRequest(_DRAW, GAME_DRAW, true);
		addValue(_GAMEID, gameid);
		addValue(_ACTION, action);
	}
}
