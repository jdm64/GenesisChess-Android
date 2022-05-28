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

package com.chess.genesis.controller;

import android.content.*;
import android.os.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import androidx.fragment.app.*;

public abstract class GameState implements IGameController, Handler.Callback
{
	public static final int PLACEOFFSET = 1000;

	final IGameFrag gamefrag;
	final FragmentActivity activity;
	final Bundle settings;
	private final ProgressMsg progress;
	final ObjectArray<Move> history;
	final IMoveHandler hintList;
	final Board board;

	final Handler handle;
	private final NetworkClient net;
	private final Engine cpu;

	private final int ycol;
	private final int type;
	private final int oppType;

	int hindex = -1;

	protected abstract void revertMove(final Move move);
	protected abstract void applyMove(final Move move, final boolean erase, final boolean localmove);

	public abstract void handleMove(final int from, final int to);

	@Override
	public boolean handleMessage(final Message msg)
	{
	try {
		switch (msg.what) {
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
				gamefrag.showToast("ERROR:\n" + json.getString("reason"));
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
				gamefrag.showToast("ERROR:\n" + json.getString("reason"));
				return true;
			}
			progress.setText("Resignation Sent");

			net.game_status(settings.getString("gameid"));
			new Thread(net).start();
			break;
		case NetworkClient.NUDGE_GAME:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error"))
				gamefrag.showToast("ERROR:\n" + json.getString("reason"));
			progress.dismiss();
			break;
		case NetworkClient.GAME_STATUS:
			json = (JSONObject) msg.obj;

			if (json.getString("result").equals("error")) {
				progress.dismiss();
				gamefrag.showToast("ERROR:\n" + json.getString("reason"));
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
				gamefrag.showToast("ERROR:\n" + json.getString("reason"));
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
				gamefrag.showToast("ERROR:\n" + json.getString("reason"));
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

	GameState(FragmentActivity act, IGameFrag gameFrag, Board _board)
	{
		gamefrag = gameFrag;
		activity = act;
		settings = gameFrag.getGameData();
		board = _board;
		handle = new Handler(this);
		history = new ObjectArray<>(board.moveGenerator());
		hintList = new HintList(gamefrag, this, board);
		progress = new ProgressMsg(activity);
		type = settings.getInt("type", Enums.ONLINE_GAME);

		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			cpu = board instanceof GenBoard? new GenEngine(board) : new RegEngine(board);
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

	void reset()
	{
		hindex = -1;
		resetPieces();
		history.clear();
		board.reset();
	}

	void resetPieces()
	{
		for (int i = PLACEOFFSET - 6; i < PLACEOFFSET; i++)
			gamefrag.getPlaceSq(i).reset();
		for (int i = PLACEOFFSET + 1; i < PLACEOFFSET + 7; i++)
			gamefrag.getPlaceSq(i).reset();
		setStm();
	}

	@Override
	public BoardView getBoardView()
	{
		return gamefrag.getBoardView();
	}

	@Override
	public CapturedLayout getCapturedView()
	{
		return gamefrag.getCapturedView();
	}

	public Board getBoard()
	{
		return board;
	}

	@Override
	public void onBackClick()
	{
		if (hindex < 0)
			return;
		revertMove(history.get(hindex));
	}

	@Override
	public void onForwardClick()
	{
		if (hindex + 1 >= history.size())
			return;
		applyMove(history.get(hindex + 1), false, true);
	}

	@Override
	public void onCurrentClick()
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

	int yourColor()
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

	void check_endgame()
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
		CpuTimeDialog.create(handle, cpu.getTime()).show(activity.getSupportFragmentManager(), "");
	}

	private boolean runCPU()
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

		Util.runThread(() -> {
			runCpu();
		});
		return true;
	}

	private void runCpu()
	{
		cpu.setBoard(board);
		var move = cpu.getMove();
		if (cpu.getTime() == 0) {
			cpu.setBoard(board);
			Util.runThread(() -> { runCpu(); });
			return;
		} else if (activity.isFinishing()) {
			// activity is gone, so give up!
			return;
		}

		onCurrentClick();

		var vMove = board.newMove();
		if (board.validMove(move, vMove))
			applyMove(vMove, true, true);
	}

	public void submitMove()
	{
		progress.setText("Sending Move");

		final String gameid = settings.getString("gameid");
		final String move = history.top().toString();

		net.submit_move(gameid, move);
		new Thread(net).start();
	}

	boolean boardNotEditable()
	{
		return type == Enums.ARCHIVE_GAME ||
			(type == Enums.ONLINE_GAME && hindex + 1 < history.size());
	}

	private void preCommonMove()
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			final int king = board.kingIndex(board.getStm());
			final IBoardSq kingI = gamefrag.getBoardSq(king);
			kingI.setCheck(false);
		}
	}

	void preApplyMove()
	{
		hintList.clear();
		if (hindex >= 0) {
			// undo last move highlight
			final IBoardSq to = gamefrag.getBoardSq(history.get(hindex).to);
			to.setLast(false);

			preCommonMove();
		}
	}

	void preRevertMove()
	{
		hintList.clear();
		preCommonMove();
	}

	private void postCommonMove()
	{
		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final IBoardSq kingI = gamefrag.getBoardSq(king);
			kingI.setCheck(true);
		}
		// set captured pieces
		gamefrag.setCapturedCounts(board.getPieceCounts(Piece.DEAD));

		setStm();
	}

	void postApplyMove()
	{
		postCommonMove();
	}

	void postRevertMove()
	{
		// redo last move highlight
		if (hindex >= 0) {
			final IBoardSq hto = gamefrag.getBoardSq(history.get(hindex).to);
			hto.setLast(true);
		}
		postCommonMove();
	}

	private void applyRemoteMove(final String hist)
	{
		if (hist == null || hist.length() < 3)
			return;

		final String[] movehistory = hist.trim().split(" +");
		final String sMove = movehistory[movehistory.length - 1];

		// don't apply duplicate moves
		if (history.size() != 0 && sMove.equals(history.top().toString()))
			return;

		// must be on most current move to apply it
		onCurrentClick();
		gamefrag.showToast("New move loaded...");

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
			gamefrag.showSubmitMove();
		case Enums.ARCHIVE_GAME:
			break;
		}
	}

	private void setStm()
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

		if (board.getStm() == Piece.WHITE) {
			gamefrag.setNameText(true, true, mate, wstr + think);
			gamefrag.setNameText(false, false, false, bstr);
		} else {
			gamefrag.setNameText(true, false, false, wstr);
			gamefrag.setNameText(false, true, mate, bstr + think);
		}
	}

	void setBoard(final int[] pieces)
	{
		for (int i = -6; i < 0; i++) {
			final IPlaceSq button = gamefrag.getPlaceSq(i + PLACEOFFSET);
			button.setCount(pieces[i + 6]);
		}
		for (int i = 1; i < 7; i++) {
			final IPlaceSq button = gamefrag.getPlaceSq(i + PLACEOFFSET);
			button.setCount(pieces[i + 6]);
		}

		// set board pieces
		final int[] squares = board.getBoardArray();
		for (int i = 0; i < 64; i++) {
			final int loc = BaseBoard.SF88(i);
			final IBoardSq button = gamefrag.getBoardSq(loc);
			button.setPiece(squares[loc]);
		}
		// set last move highlight
		if (history.size() != 0) {
			final IBoardSq to = gamefrag.getBoardSq(history.top().to);
			to.setLast(true);
		}

		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final IBoardSq kingI = gamefrag.getBoardSq(king);
			kingI.setCheck(true);
		}
		// set captured pieces
		gamefrag.setCapturedCounts(board.getPieceCounts(Piece.DEAD));

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
			gamefrag.showToast("Game must be idle before nudging");
			break;
		case Enums.IDLE:
			new NudgeConfirm(activity, handle).show();
			break;
		case Enums.NUDGED:
			gamefrag.showToast("Game is already nudged");
			break;
		case Enums.CLOSE:
			new IdleResignConfirm(activity, handle).show();
			break;
		}
	}

	public void rematch()
	{
		final String opp = settings.getString(settings.getString("username").equals(settings.getString("white")) ? "black" : "white");
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

	@Override
	public void onBoardLongClick(IBoardSq sq)
	{
		hintList.onBoardLongClick(sq, yourColor());
	}

	@Override
	public void onPlaceClick()
	{
		gamefrag.togglePlaceBoard();
	}
}
