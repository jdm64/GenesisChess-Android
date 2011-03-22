package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import java.io.IOException;
import java.lang.Runnable;
import java.net.SocketException;
import org.json.JSONException;
import org.json.JSONObject;

class NetworkClient implements Runnable
{
	public final static int NONE = 0;
	public final static int LOGIN = 1;
	public final static int REGISTER = 2;
	public final static int JOIN_GAME = 3;
	public final static int NEW_GAME = 4;
	public final static int READ_INBOX = 5;
	public final static int CLEAR_INBOX = 6;
	public final static int GAME_STATUS = 7;
	public final static int GAME_INFO = 8;
	public final static int SUBMIT_MOVE = 9;
	public final static int SUBMIT_MSG = 10;
	public final static int SYNC_GAMIDS = 11;
	public final static int GAME_SCORE = 12;
	public final static int GAME_DATA = 13;

	private Context context;
	private Handler callback;
	private JSONObject json;

	private int fid = NONE;
	private boolean loginRequired;
	private boolean error = false;

	public NetworkClient(Context _context, Handler handler)
	{
		callback = handler;
		context = _context;
	}

	private boolean relogin(SocketClient net)
	{
		if (SocketClient.isLoggedin)
			return true;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String username = settings.getString("username", "!error!");
		String password = settings.getString("passhash", "!error!");

		JSONObject json2 = new JSONObject();

		try {
			try {
				json2.put("request", "login");
				json2.put("username", username);
				json2.put("passhash", Crypto.LoginKey(password));
			} catch (JSONException e) {
				throw new RuntimeException();
			}
		} catch (SocketException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Can't contact server for sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		} catch (IOException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Lost connection durring sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		}
		if (error) {
			callback.sendMessage(Message.obtain(callback, fid, json2));
			error = false;
			return false;
		}

		try {
			net.write(json2);
		} catch (SocketException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Can't contact server for sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		} catch (IOException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Lost connection durring sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		}
		if (error) {
			callback.sendMessage(Message.obtain(callback, fid, json2));
			error = false;
			return false;
		}

		try {
			json2 = net.read();
		} catch (SocketException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Can't contact server for recieving data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
		} catch (IOException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Lost connection durring recieving data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
		} catch (JSONException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Server response illogical");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
		}
		if (error) {
			callback.sendMessage(Message.obtain(callback, fid, json2));
			error = false;
			return false;
		}

		try {
			if (!json2.getString("result").equals("ok")) {
				callback.sendMessage(Message.obtain(callback, fid, json2));
				return false;
			}
			SocketClient.isLoggedin = true;
			return true;
		} catch (JSONException e) {
			return false;
		}
	}

	public void run()
	{
		SocketClient net = new SocketClient();
		JSONObject json2 = null;

		if (error) {
			error = false;
			return;
		}
		if (loginRequired) {
			if (!relogin(net)) {
				net.disconnect();
				return;
			}
		}

		try {
			net.write(json);
		} catch (SocketException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Can't contact server for sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		} catch (IOException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Lost connection durring sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		}
		if (error) {
			callback.sendMessage(Message.obtain(callback, fid, json2));
			error = false;
			return;
		}

		try {
			json2 = net.read();
		} catch (SocketException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Can't contact server for recieving data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
		} catch (IOException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Lost connection durring recieving data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
		} catch (JSONException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Server response illogical");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
		}
		if (error)
			error = false;
		callback.sendMessage(Message.obtain(callback, fid, json2));
		net.disconnect();
	}

	public void register(String username, String password, String email)
	{
		fid = REGISTER;
		loginRequired = false;

		json = new JSONObject();

		try {
			json.put("request", "register");
			json.put("username", username);
			json.put("passhash", Crypto.HashPasswd(password));
			json.put("email", email);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void login_user(String username, String password)
	{
		fid = LOGIN;
		loginRequired = false;

		JSONObject json2 = null;
		json = new JSONObject();

		try {
			try {
				json.put("request", "login");
				json.put("username", username);
				json.put("passhash", Crypto.LoginKey(password));
			} catch (JSONException e) {
				throw new RuntimeException();
			}
		} catch (SocketException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Can't contact server for sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		} catch (IOException e) {
			json2 = new JSONObject();
			try {
				json2.put("result", "error");
				json2.put("reason", "Lost connection durring sending data");
			} catch (Throwable t) {
				throw new RuntimeException();
			}
			error = true;
		}
		if (error) {
			callback.sendMessage(Message.obtain(callback, fid, json2));
			error = true;
		}
	}

	public void join_game(String username, String gametype)
	{
		fid = JOIN_GAME;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "joingame");
			json.put("username", username);
			json.put("gametype", gametype);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void new_game(String username, String opponent, String gametype, String color)
	{
		fid = NEW_GAME;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "newgame");
			json.put("username", username);
			json.put("opponent", opponent);
			json.put("gametype", gametype);
			json.put("color", color);
		} catch (Throwable t) {
			throw new RuntimeException();
		}

	}

	public void submit_move(String username, String gameid, String move)
	{
		fid = SUBMIT_MOVE;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "sendmove");
			json.put("username", username);
			json.put("gameid", gameid);
			json.put("move", move);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void submit_msg(String username, String gameid, String msg)
	{
		fid = SUBMIT_MSG;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "sendmsg");
			json.put("username", username);
			json.put("gameid", gameid);
			json.put("msg", msg);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void game_status(String username, String gameid)
	{
		fid = GAME_STATUS;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "gamestatus");
			json.put("username", username);
			json.put("gameid", gameid);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void game_info(String username, String gameid)
	{
		fid = GAME_INFO;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "gameinfo");
			json.put("username", username);
			json.put("gameid", gameid);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void game_data(String username, String gameid)
	{
		fid = GAME_DATA;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "gamedata");
			json.put("username", username);
			json.put("gameid", gameid);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void read_inbox(String username)
	{
		fid = READ_INBOX;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "inbox");
			json.put("username", username);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void clear_inbox(String username, long time)
	{
		fid = CLEAR_INBOX;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "clearinbox");
			json.put("username", username);
			json.put("time", time);
		} catch (Throwable t) {
			throw new RuntimeException();
		}
	}

	public void sync_gameids(String username, String type)
	{
		fid = SYNC_GAMIDS;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "gameids");
			json.put("type", type);
			json.put("username", username);
		} catch (Throwable e) {
			throw new RuntimeException();
		}
	}

	public void game_score(String username, String gameid)
	{
		fid = GAME_SCORE;
		loginRequired = true;

		json = new JSONObject();

		try {
			json.put("request", "gamescore");
			json.put("username", username);
			json.put("gameid", gameid);
		} catch (Throwable e) {
			throw new RuntimeException();
		}
	}
}
