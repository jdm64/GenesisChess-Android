package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

abstract class GameState
{
	public static GameState self;

	protected Handler handle;
	protected Context context;
	protected Bundle settings;

	protected NetworkClient net;
	protected ProgressMsg progress;
	protected Engine cpu;
	protected IntArray callstack;

	protected int ycol;
	protected int type;
	protected int oppType;
	protected int hindex = -1;

	protected void check_endgame()
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			return;
		case Enums.ONLINE_GAME:
			if (Integer.valueOf(settings.getString("status")) == Enums.ACTIVE) {
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

				(new GameStatsDialog(context, json)).show();
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
			(new GameStatsDialog(context, settings)).show();
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
		(new CpuTimeDialog(context, handle, cpu.getTime())).show();
	}

	public void nudge_resign()
	{
		final GameDataDB db = new GameDataDB(context);
		final Bundle data = db.getOnlineGameData(settings.getString("gameid"));
		db.close();

		final int yturn = Integer.valueOf(data.getString("yourturn"));
		if (yturn == Enums.YOUR_TURN) {
			(new ResignConfirm(context, handle)).show();
			return;
		}

		switch (Integer.valueOf(data.getString("idle"))) {
		default:
		case Enums.NOTIDLE:
			Toast.makeText(context, "Game must be idle before nudging", Toast.LENGTH_LONG).show();
			break;
		case Enums.IDLE:
			(new NudgeConfirm(context, handle)).show();
			break;
		case Enums.NUDGED:
			Toast.makeText(context, "Game is already nudged", Toast.LENGTH_LONG).show();
			break;
		case Enums.CLOSE:
			(new IdleResignConfirm(context, handle)).show();
			break;
		}
	}

	public void rematch()
	{
		final String opp = settings.getString("username").equals(settings.getString("white"))?
			settings.getString("black") : settings.getString("white");
		(new RematchConfirm(context, handle, opp)).show();
	}

	public abstract void boardClick(final View v);

	public abstract void placeClick(final View v);
}
