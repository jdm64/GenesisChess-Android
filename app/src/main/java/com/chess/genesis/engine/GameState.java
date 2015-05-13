/* GenChess, a genesis chess engine
 * Copyright (C) 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chess.genesis.engine;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import org.json.*;

public abstract class GameState implements Handler.Callback
{
	protected final GameFrag game;
	protected final Activity activity;
	protected final Bundle settings;
	protected final ProgressMsg progress;
	protected final ObjectArray<Move> history;
	protected final HintList hintList;
	protected final Board board;

	protected final Handler handle;
	protected final NetworkClient net;
	protected final Engine cpu;

	protected final int ycol;
	protected final int type;
	protected final int oppType;

	protected int hindex = -1;

	protected abstract void revertMove(final Move move);
	protected abstract void applyMove(final Move move, final boolean erase, final boolean localmove);

	public abstract void setBoard();
	public abstract void boardClick(final IBoardSq sq);
	public abstract void placeClick(final IPlaceSq sq);
	public abstract void handleMove(final int from, final int to);

	@Override
	public boolean handleMessage(final Message msg)
	{
	try {
		switch (msg.what) {
		case GenEngine.MSG:
		case RegEngine.MSG:
			final Bundle bundle = (Bundle) msg.obj;

			if (bundle.getLong("time") == 0) {
				cpu.setBoard(board);
				new Thread(cpu).start();
				return true;
			} else if (activity.isFinishing()) {
				// activity is gone, so give up!
				return true;
			}
			currentMove();

			final Move tmove = bundle.getParcelable("move");
			final Move move = board.newMove();
			if (board.validMove(tmove, move))
				applyMove(move, true, true);
			break;
		case CpuTimeDialog.MSG:
			final PrefEdit pref = new PrefEdit(activity);
			pref.putInt(R.array.pf_cputime, (Integer) msg.obj);
			pref.commit();
			cpu.setTime((Integer) msg.obj);
			break;
		case NetworkClient.GAME_DRAW:
		case NetworkClient.SUBMIT_MOVE:
			JSONObject json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				undoMove();
				progress.dismiss();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return true;
			}
			progress.setText("Checking Game Status");

			net.game_status(settings.getString("gameid"));
			new Thread(net).start();
			break;
		case ResignConfirm.MSG:
			progress.setText("Sending Resignation");

			net.resign_game(settings.getString("gameid"));
			new Thread(net).start();
			break;
		case NudgeConfirm.MSG:
			progress.setText("Sending Nudge");

			net.nudge_game(settings.getString("gameid"));
			new Thread(net).start();
			break;
		case IdleResignConfirm.MSG:
			progress.setText("Sending Idle Resign");

			net.idle_resign(settings.getString("gameid"));
			new Thread(net).start();
			break;
		case DrawDialog.MSG:
		case AcceptDrawDialog.MSG:
			final String value = (String) msg.obj;
			progress.setText("Sending Draw");

			net.game_draw(settings.getString("gameid"), value);
			new Thread(net).start();
			break;
		case PawnPromoteDialog.MSG:
			applyMove((RegMove) msg.obj, true, true);
			break;
		case NetworkClient.RESIGN_GAME:
		case NetworkClient.IDLE_RESIGN:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.dismiss();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return true;
			}
			progress.setText("Resignation Sent");

			net.game_status(settings.getString("gameid"));
			new Thread(net).start();
			break;
		case NetworkClient.NUDGE_GAME:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error"))
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
			progress.dismiss();
			break;
		case NetworkClient.GAME_STATUS:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.dismiss();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return true;
			}
			final int status = Enums.GameStatus(json.getString("status"));

			settings.putString("status", String.valueOf(status));

			final GameDataDB db = new GameDataDB(activity);
			db.updateOnlineGame(json);
			db.close();
			GenesisNotifier.clearNotification(activity, GenesisNotifier.YOURTURN_NOTE);

			applyRemoteMove(json.getString("history"));
			if (status != Enums.ACTIVE) {
				if (Integer.parseInt(settings.getString("eventtype")) == Enums.INVITE) {
					progress.dismiss();
					ShowGameStats(json);
					return true;
				}
				progress.setText("Retrieving Score");

				net.game_score(settings.getString("gameid"));
				new Thread(net).start();
			} else {
				progress.setText("Status Synced");
				progress.dismiss();
			}
			break;
		case NetworkClient.GAME_SCORE:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.dismiss();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return true;
			}
			progress.setText("Score Loaded");
			progress.dismiss();

			ShowGameStats(json);
			break;
		case RematchConfirm.MSG:
			final Bundle data = (Bundle) msg.obj;
			progress.setText("Sending Newgame Request");

			final String opponent = data.getString("opp_name");
			final String color = Enums.ColorType(data.getInt("color"));
			final String gametype = Enums.GameType(data.getInt("gametype"));

			net.new_game(opponent, gametype, color);
			new Thread(net).start();
			break;
		case NetworkClient.NEW_GAME:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.dismiss();
				Toast.makeText(activity, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return true;
			}
			progress.setText(json.getString("reason"));
			progress.dismiss();
			break;
		}
		return true;
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	public GameState(final GameFrag _game, final Board _board)
	{
		game = _game;
		activity = game.getActivity();
		settings = game.getArguments();
		board = _board;
		handle = new Handler(this);
		history = new ObjectArray<>(board.moveGenerator());
		hintList = new HintList(activity, this, board);
		progress = new ProgressMsg(activity);
		type = settings.getInt("type", Enums.ONLINE_GAME);

		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			cpu = board instanceof GenBoard? new GenEngine(handle, board) : new RegEngine(handle, board);
			cpu.setTime(Pref.getInt(activity, R.array.pf_cputime));
			oppType = Integer.parseInt(settings.getString("opponent"));
			net = null;
			ycol = (oppType == Enums.CPU_WHITE_OPPONENT)? Piece.BLACK : Piece.WHITE;
			break;
		case Enums.ONLINE_GAME:
		case Enums.ARCHIVE_GAME:
			oppType = Enums.HUMAN_OPPONENT;
			cpu = null;
			net = new NetworkClient(activity, handle);
			ycol = settings.getString("username").equals(settings.getString("white"))? Piece.WHITE : Piece.BLACK;
			break;
		}
	}

	public void reset()
	{
		hindex = -1;
		game.reset();
		history.clear();
		board.reset();
	}

	public void backMove()
	{
		if (hindex < 0)
			return;
		revertMove(history.get(hindex));
	}

	public void forwardMove()
	{
		if (hindex + 1 >= history.size())
			return;
		applyMove(history.get(hindex + 1), false, true);
	}

	public void currentMove()
	{
		final int len = history.size();
		while (hindex + 1 < len)
			applyMove(history.get(hindex + 1), false, true);
	}

	public void firstMove()
	{
		while (hindex > 0)
			revertMove(history.get(hindex));
	}

	public void undoMove()
	{
		if (hindex < 0)
			return;
		revertMove(history.get(hindex));
		history.pop();
	}

	protected int yourColor()
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			if (oppType == Enums.HUMAN_OPPONENT)
				return board.getStm();
		case Enums.ONLINE_GAME:
			return ycol;
		case Enums.ARCHIVE_GAME:
			return board.getStm();
		default:
			return 0;
		}
	}

	protected void check_endgame()
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			return;
		case Enums.ONLINE_GAME:
			if (Integer.parseInt(settings.getString("status")) == Enums.ACTIVE) {
				// check for draw offers and pending draws
				if (Integer.parseInt(settings.getString("drawoffer")) * ycol < 0)
					new AcceptDrawDialog(activity, handle).show();
				else if (Integer.parseInt(settings.getString("drawoffer")) * ycol > 0)
					new PendingDrawDialog(activity).show();
				return;
			} else if (Integer.parseInt(settings.getString("eventtype")) == Enums.INVITE) {
			try {
				ShowGameStats(new JSONObject());
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			} else {
				progress.setText("Retrieving Score");
				net.game_score(settings.getString("gameid"));
				new Thread(net).start();
			}
			break;
		case Enums.ARCHIVE_GAME:
			settings.putInt("yourcolor", ycol);
			new GameStatsDialog(activity, settings).show();
			break;
		}
	}

	public Bundle getBundle()
	{
		return settings;
	}

	public void resync()
	{
		progress.setText("Updating Game State");
		net.game_status(settings.getString("gameid"));
		new Thread(net).start();
	}

	public void setCpuTime()
	{
		new CpuTimeDialog(activity, handle, cpu.getTime()).show();
	}

	protected boolean runCPU()
	{
		// Start computer player
		if (oppType == Enums.HUMAN_OPPONENT)
			return false;
		else if (hindex + 1 < history.size())
			return false;
		else if (board.getStm() == ycol)
			return false;

		if (cpu.isActive()) {
			cpu.stop();
			return true;
		}
		cpu.setBoard(board);
		new Thread(cpu).start();
		return true;
	}

	public void submitMove()
	{
		progress.setText("Sending Move");

		final String gameid = settings.getString("gameid");
		final String move = history.top().toString();

		net.submit_move(gameid, move);
		new Thread(net).start();
	}

	protected boolean boardNotEditable()
	{
		return type == Enums.ARCHIVE_GAME ||
			(type == Enums.ONLINE_GAME && hindex + 1 < history.size());
	}

	private void preCommonMove()
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			final int king = board.kingIndex(board.getStm());
			final IBoardSq kingI = (IBoardSq) activity.findViewById(king);
			kingI.setCheck(false);
		}
	}

	protected void preApplyMove()
	{
		hintList.clear();
		if (hindex >= 0) {
			// undo last move highlight
			final IBoardSq to = (IBoardSq) activity.findViewById(history.get(hindex).to);
			to.setLast(false);

			preCommonMove();
		}
	}

	protected void preRevertMove()
	{
		hintList.clear();
		preCommonMove();
	}

	private void postCommonMove()
	{
		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final IBoardSq kingI = (IBoardSq) activity.findViewById(king);
			kingI.setCheck(true);
		}
		// set captured pieces
		game.captured_count.setPieces(board.getPieceCounts(Piece.DEAD));

		setStm();
	}

	protected void postApplyMove()
	{
		postCommonMove();
	}

	protected void postRevertMove()
	{
		// redo last move highlight
		if (hindex >= 0) {
			final IBoardSq hto = (IBoardSq) activity.findViewById(history.get(hindex).to);
			hto.setLast(true);
		}
		postCommonMove();
	}

	protected void applyRemoteMove(final String hist)
	{
		if (hist == null || hist.length() < 3)
			return;

		final String[] movehistory = hist.trim().split(" +");
		final String sMove = movehistory[movehistory.length - 1];

		// don't apply duplicate moves
		if (history.size() != 0 && sMove.equals(history.top().toString()))
			return;

		// must be on most current move to apply it
		currentMove();
		Toast.makeText(activity, "New move loaded...", Toast.LENGTH_LONG).show();

		final Move move = board.newMove();
		move.parse(sMove);
		if (board.validMove(move) != Move.VALID_MOVE)
			return;
		applyMove(move, true, false);
	}

	public void save(final Context context, final boolean exitgame)
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			final GameDataDB db = new GameDataDB(context);
			final int id = Integer.parseInt(settings.getString("id"));

			if (history.size() < 1) {
				db.deleteLocalGame(id);
				db.close();
				return;
			} else if (exitgame) {
				db.close();
				return;
			}
			final long stime = System.currentTimeMillis();
			final String zfen = board.printZfen();
			final String hist = history.toString();

			// save update local game data bundle
			settings.putString("history", hist);
			settings.putString("stime", String.valueOf(stime));
			settings.putString("zfen", zfen);

			db.saveLocalGame(id, stime, zfen, hist);
			db.close();
			break;
		case Enums.ONLINE_GAME:
			if (exitgame)
				return;
			game.displaySubmitMove();
		case Enums.ARCHIVE_GAME:
			break;
		}
	}

	public final void setStm()
	{
		String think = "", wstr, bstr;
		boolean mate = false;

		switch (board.isMate()) {
		case Move.NOT_MATE:
		default:
			if (runCPU())
				think = " thinking";
			break;
		case Move.CHECK_MATE:
		case Move.STALE_MATE:
			mate = true;
			break;
		}
		if (type == Enums.LOCAL_GAME) {
			wstr = "White";
			bstr = "Black";
		} else {
			wstr = settings.getString("white");
			bstr = settings.getString("black");
		}

		final TabText white = (TabText) activity.findViewById(R.id.white_name);
		final TabText black = (TabText) activity.findViewById(R.id.black_name);

		if (board.getStm() == Piece.WHITE) {
			white.setText(wstr + think);
			white.setActive(true);

			black.setText(bstr);
			black.setActive(false);

			if (mate)
				white.setTabTextColor(MColors.RED_A700);
		} else {
			white.setText(wstr);
			white.setActive(false);

			black.setText(bstr + think);
			black.setActive(true);

			if (mate)
				black.setTabTextColor(MColors.RED_A700);
		}
	}

	protected void setBoard(final int[] pieces)
	{
		for (int i = -6; i < 0; i++) {
			final IPlaceSq button = (IPlaceSq) activity.findViewById(i + 1000);
			button.setCount(pieces[i + 6]);
		}
		for (int i = 1; i < 7; i++) {
			final IPlaceSq button = (IPlaceSq) activity.findViewById(i + 1000);
			button.setCount(pieces[i + 6]);
		}

		// set board pieces
		final int[] squares = board.getBoardArray();
		for (int i = 0; i < 64; i++) {
			final int loc = BaseBoard.SF88(i);
			final IBoardSq button = (IBoardSq) activity.findViewById(loc);
			button.setPiece(squares[loc]);
		}
		// set last move highlight
		if (history.size() != 0) {
			final IBoardSq to = (IBoardSq) activity.findViewById(history.top().to);
			to.setLast(true);
		}

		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final IBoardSq kingI = (IBoardSq) activity.findViewById(king);
			kingI.setCheck(true);
		}
		// set captured pieces
		game.captured_count.setPieces(board.getPieceCounts(Piece.DEAD));

		setStm();
	}

	public void nudge_resign()
	{
		final GameDataDB db = new GameDataDB(activity);
		final Bundle data = db.getOnlineGameData(settings.getString("gameid"));
		db.close();

		final int yturn = Integer.parseInt(data.getString("yourturn"));
		if (yturn == Enums.YOUR_TURN) {
			new ResignConfirm(activity, handle).show();
			return;
		}

		switch (Integer.parseInt(data.getString("idle"))) {
		default:
		case Enums.NOTIDLE:
			Toast.makeText(activity, "Game must be idle before nudging", Toast.LENGTH_LONG).show();
			break;
		case Enums.IDLE:
			new NudgeConfirm(activity, handle).show();
			break;
		case Enums.NUDGED:
			Toast.makeText(activity, "Game is already nudged", Toast.LENGTH_LONG).show();
			break;
		case Enums.CLOSE:
			new IdleResignConfirm(activity, handle).show();
			break;
		}
	}

	public void rematch()
	{
		final String opp = settings.getString("username").equals(settings.getString("white"))?
			settings.getString("black") : settings.getString("white");
		new RematchConfirm(activity, handle, opp).show();
	}

	public void draw()
	{
		new DrawDialog(activity, handle).show();
	}

	private void ShowGameStats(final JSONObject json) throws JSONException
	{
		json.put("yourcolor", ycol);
		json.put("white_name", settings.getString("white"));
		json.put("black_name", settings.getString("black"));
		json.put("eventtype", settings.getString("eventtype"));
		json.put("status", settings.getString("status"));
		json.put("gametype", Enums.GameType(Integer.parseInt(settings.getString("gametype"))));
		json.put("gameid", settings.getString("gameid"));

		new GameStatsDialog(activity, json).show();
	}

	public boolean boardLongClick(final IBoardSq sq)
	{
		hintList.longBoardClick(sq, yourColor());
		return true;
	}
}
