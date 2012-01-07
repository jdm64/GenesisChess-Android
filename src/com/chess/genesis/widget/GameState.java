package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

abstract class GameState
{
	protected Handler handle;
	protected Game activity;
	protected Bundle settings;

	protected NetworkClient net;
	protected ProgressMsg progress;
	protected Engine cpu;
	protected IntArray callstack;

	protected int ycol;
	protected int type;
	protected int oppType;
	protected int hindex = -1;

	protected abstract void applyRemoteMove(final String hist);

	public abstract void firstMove();
	public abstract void currentMove();
	public abstract void forwardMove();
	public abstract void undoMove();
	public abstract void backMove();
	public abstract void submitMove();

	public abstract void setStm();
	public abstract void setBoard();

	public abstract void save(final Context context, final boolean exitgame);
	public abstract Bundle getBundle();

	public abstract void boardClick(final View v);
	public abstract void placeClick(final View v);

	protected void handleOther(final Message msg)
	{
	try {
		switch (msg.what) {
		case CpuTimeDialog.MSG:
			final Editor pref = PreferenceManager.getDefaultSharedPreferences(activity).edit();
			pref.putInt("cputime", (Integer) msg.obj);
			pref.commit();
			cpu.setTime((Integer) msg.obj);
			break;
		case NetworkClient.GAME_DRAW:
		case NetworkClient.SUBMIT_MOVE:
			JSONObject json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				undoMove();
				progress.remove();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			progress.setText("Checking Game Status");

			net.game_status(settings.getString("gameid"));
			(new Thread(net)).start();
			break;
		case ResignConfirm.MSG:
			progress.setText("Sending Resignation");

			net.resign_game(settings.getString("gameid"));
			(new Thread(net)).start();
			break;
		case NudgeConfirm.MSG:
			progress.setText("Sending Nudge");

			net.nudge_game(settings.getString("gameid"));
			(new Thread(net)).start();
			break;
		case IdleResignConfirm.MSG:
			progress.setText("Sending Idle Resign");

			net.idle_resign(settings.getString("gameid"));
			(new Thread(net)).start();
			break;
		case DrawDialog.MSG:
		case AcceptDrawDialog.MSG:
			final String value = (String) msg.obj;
			progress.setText("Sending Draw");

			net.game_draw(settings.getString("gameid"), value);
			(new Thread(net)).start();
			break;
		case NetworkClient.RESIGN_GAME:
		case NetworkClient.IDLE_RESIGN:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			progress.setText("Resignation Sent");

			net.game_status(settings.getString("gameid"));
			(new Thread(net)).start();
			break;
		case NetworkClient.NUDGE_GAME:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error"))
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
			progress.remove();
			break;
		case NetworkClient.GAME_STATUS:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			final String history = json.getString("history");
			final int status = Enums.GameStatus(json.getString("status"));

			settings.putString("status", String.valueOf(status));

			final GameDataDB db = new GameDataDB(activity);
			db.updateOnlineGame(json);
			db.close();
			GenesisNotifier.clearNotification(activity, GenesisNotifier.YOURTURN_NOTE);

			applyRemoteMove(history);
			if (status != Enums.ACTIVE) {
				if (Integer.valueOf(settings.getString("eventtype")) == Enums.INVITE) {
					progress.remove();

					json.put("yourcolor", ycol);
					json.put("white_name", settings.getString("white"));
					json.put("black_name", settings.getString("black"));
					json.put("eventtype", settings.getString("eventtype"));
					json.put("status", settings.getString("status"));
					json.put("gametype", Enums.GameType(Integer.valueOf(settings.getString("gametype"))));
					json.put("gameid", settings.getString("gameid"));

					(new GameStatsDialog(activity, json)).show();
					return;
				}
				progress.setText("Retrieving Score");

				net.game_score(settings.getString("gameid"));
				(new Thread(net)).start();
			} else {
				progress.setText("Status Synced");
				progress.remove();
			}
			break;
		case NetworkClient.GAME_SCORE:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			progress.setText("Score Loaded");
			progress.remove();

			json.put("yourcolor", ycol);
			json.put("white_name", settings.getString("white"));
			json.put("black_name", settings.getString("black"));
			json.put("eventtype", settings.getString("eventtype"));
			json.put("status", settings.getString("status"));
			json.put("gametype", Enums.GameType(Integer.valueOf(settings.getString("gametype"))));
			json.put("gameid", settings.getString("gameid"));

			(new GameStatsDialog(activity, json)).show();
			break;
		case RematchConfirm.MSG:
			final Bundle data = (Bundle) msg.obj;
			progress.setText("Sending Newgame Request");

			final String opponent = data.getString("opp_name");
			final String color = Enums.ColorType(data.getInt("color"));
			final String gametype = Enums.GameType(data.getInt("gametype"));

			net.new_game(opponent, gametype, color);
			(new Thread(net)).start();
			break;
		case NetworkClient.NEW_GAME:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			progress.setText(json.getString("reason"));
			progress.remove();
			break;
		}
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	public void reset()
	{
		hindex = -1;
		callstack.clear();
		activity.reset();
	}

	protected void check_endgame()
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			return;
		case Enums.ONLINE_GAME:
			if (Integer.valueOf(settings.getString("status")) == Enums.ACTIVE) {
				// check for draw offers and pending draws
				if (Integer.valueOf(settings.getString("drawoffer")) * ycol < 0)
					(new AcceptDrawDialog(activity, handle)).show();
				else if (Integer.valueOf(settings.getString("drawoffer")) * ycol > 0)
					(new PendingDrawDialog(activity)).show();
				return;
			} else if (Integer.valueOf(settings.getString("eventtype")) == Enums.INVITE) {
			try {
				final JSONObject json = new JSONObject();
				json.put("yourcolor", ycol);
				json.put("white_name", settings.getString("white"));
				json.put("black_name", settings.getString("black"));
				json.put("eventtype", settings.getString("eventtype"));
				json.put("status", settings.getString("status"));
				json.put("gametype", Enums.GameType(Integer.valueOf(settings.getString("gametype"))));
				json.put("gameid", settings.getString("gameid"));

				(new GameStatsDialog(activity, json)).show();
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
			} else {
				progress.setText("Retrieving Score");
				net.game_score(settings.getString("gameid"));
				(new Thread(net)).start();
			}
			break;
		case Enums.ARCHIVE_GAME:
			settings.putInt("yourcolor", ycol);
			(new GameStatsDialog(activity, settings)).show();
			break;
		}
	}

	public void resync()
	{
		progress.setText("Updating Game State");
		net.game_status(settings.getString("gameid"));
		(new Thread(net)).start();
	}

	public void setCpuTime()
	{
		(new CpuTimeDialog(activity, handle, cpu.getTime())).show();
	}

	public void nudge_resign()
	{
		final GameDataDB db = new GameDataDB(activity);
		final Bundle data = db.getOnlineGameData(settings.getString("gameid"));
		db.close();

		final int yturn = Integer.valueOf(data.getString("yourturn"));
		if (yturn == Enums.YOUR_TURN) {
			(new ResignConfirm(activity, handle)).show();
			return;
		}

		switch (Integer.valueOf(data.getString("idle"))) {
		default:
		case Enums.NOTIDLE:
			Toast.makeText(activity, "Game must be idle before nudging", Toast.LENGTH_LONG).show();
			break;
		case Enums.IDLE:
			(new NudgeConfirm(activity, handle)).show();
			break;
		case Enums.NUDGED:
			Toast.makeText(activity, "Game is already nudged", Toast.LENGTH_LONG).show();
			break;
		case Enums.CLOSE:
			(new IdleResignConfirm(activity, handle)).show();
			break;
		}
	}

	public void rematch()
	{
		final String opp = settings.getString("username").equals(settings.getString("white"))?
			settings.getString("black") : settings.getString("white");
		(new RematchConfirm(activity, handle, opp)).show();
	}

	public void draw()
	{
		(new DrawDialog(activity, handle)).show();
	}
}
