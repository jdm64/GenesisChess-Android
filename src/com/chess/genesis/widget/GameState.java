package com.chess.genesis;

import android.os.Bundle;
import android.os.Handler;
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

	public abstract void boardClick(final View v);

	public abstract void placeClick(final View v);
}
