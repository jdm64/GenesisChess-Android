package com.chess.genesis;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

class GenGameState extends GameState
{
	public static GenGameState self;

	private final Context context;
	private final Bundle settings;
	private final NetworkClient net;
	private final ProgressMsg progress;
	private final ObjectArray<GenMove> history;
	private final GenBoard board;
	private final IntArray callstack;
	private final GenEngine cpu;
	private final int ycol;
	private final int type;
	private final int oppType;

	private int hindex = -1;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
		try {
			switch (msg.what) {
			case CpuTimeDialog.MSG:
				final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
				pref.putInt("cputime", (Integer) msg.obj);
				pref.commit();
				cpu.setTime((Integer) msg.obj);
				break;
			case GenEngine.MSG:
				final Bundle bundle = (Bundle) msg.obj;

				if (bundle.getLong("time") == 0) {
					cpu.setBoard(board);
					(new Thread(cpu)).start();
					return;
				}
				final GenMove move = bundle.getParcelable("move");

				currentMove();
				applyMove(move, true, true);
				break;
			case NetworkClient.SUBMIT_MOVE:
				JSONObject json = (JSONObject) msg.obj;

				if (json.getString("result").equals("error")) {
					progress.remove();
					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return;
				}
				progress.setText(json.getString("reason"));

				net.game_status(settings.getString("gameid"));
				(new Thread(net)).start();
				break;
			case ResignConfirm.MSG:
				progress.setText("Sending Resignation");

				net.resign_game(settings.getString("gameid"));
				(new Thread(net)).start();
				break;
			case NetworkClient.RESIGN_GAME:
				json = (JSONObject) msg.obj;

				if (json.getString("result").equals("error")) {
					progress.remove();
					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return;
				}
				progress.setText("Resignation Sent");

				net.game_status(settings.getString("gameid"));
				(new Thread(net)).start();
				break;
			case NetworkClient.GAME_STATUS:
				json = (JSONObject) msg.obj;

				if (json.getString("result").equals("error")) {
					progress.remove();
					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return;
				}
				final String history = json.getString("history");
				final int status = Enums.GameStatus(json.getString("status"));

				settings.putString("status", String.valueOf(status));

				final GameDataDB db = new GameDataDB(context);
				db.updateOnlineGame(json);

				// clear notification if it's not your turn in any game
				if (db.getOnlineGameList(1).getCount() == 0) {
					final NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
					nm.cancelAll();
				}
				db.close();

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

						(new EndGameDialog(context, json)).show();
						return;
					}
					progress.setText("Retrieving Score");

					settings.putString("status", String.valueOf(status));
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
					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
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

				(new EndGameDialog(context, json)).show();
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
				try {
					if (json.getString("result").equals("error")) {
						progress.remove();
						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
					progress.setText(json.getString("reason"));
					progress.remove();
				} catch (JSONException e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		}
	};

	private void check_endgame()
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

				(new EndGameDialog(context, json)).show();
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

	public GenGameState(final Context _context, final Bundle _settings)
	{
		self = this;
		GameState.self = this;
		context = _context;
		settings = _settings;

		callstack = new IntArray();
		history = new ObjectArray<GenMove>();
		board = new GenBoard();
		progress = new ProgressMsg(context);

		type = settings.getInt("type", Enums.ONLINE_GAME);
		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			cpu = new GenEngine(handle);
			cpu.setTime(pref.getInt("cputime", cpu.getTime()));
			oppType = Integer.valueOf(settings.getString("opponent"));
			net = null;
			ycol = (oppType == Enums.CPU_WHITE_OPPONENT)? Piece.BLACK : Piece.WHITE;
			break;
		case Enums.ONLINE_GAME:
		case Enums.ARCHIVE_GAME:
			oppType = Enums.HUMAN_OPPONENT;
			cpu = null;
			net = new NetworkClient(context, handle);
			ycol = settings.getString("username").equals(settings.getString("white"))? 1 : -1;
			break;
		}

		final String tmp = settings.getString("history");
		if (tmp == null || tmp.length() < 3) {
			setStm();
			check_endgame();
			return;
		}
		final String[] movehistory = tmp.trim().split(" +");

		for (int i = 0; i < movehistory.length; i++) {
			final GenMove move = new GenMove();
			move.parse(movehistory[i]);

			if (board.validMove(move) != GenBoard.VALID_MOVE)
				break;
			board.make(move);
			history.push(move);
			hindex++;
		}
		setBoard();
		check_endgame();
	}

	private int yourColor()
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

	private void setBoard()
	{
		// set place piece counts
		final int[] pieces = board.getPieceCounts();
		for (int i = -6; i < 0; i++) {
			final PlaceButton button = (PlaceButton) GenGame.self.findViewById(i + 100);
			button.setCount(pieces[i + 6]);
		}
		for (int i = 1; i < 7; i++) {
			final PlaceButton button = (PlaceButton) GenGame.self.findViewById(i + 100);
			button.setCount(pieces[i + 6]);
		}

		// set board pieces
		final int[] squares = board.getBoardArray();
		for (int i = 0; i < 64; i++) {
			final BoardButton button = (BoardButton) GenGame.self.findViewById(i);
			button.setPiece(squares[i]);
		}
		// set last move highlight
		final BoardButton to = (BoardButton) GenGame.self.findViewById(history.top().to);
		to.setLast(true);

		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) GenGame.self.findViewById(king);
			kingI.setCheck(true);
		}
		setStm();
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

		// set cpu thinking text
		final int cpuCol = (board.getStm() == Piece.WHITE)? R.id.white_name : R.id.black_name;
		final TextView txt = (TextView) GenGame.self.findViewById(cpuCol);
		txt.setText(txt.getText().toString() + " thinking");

		if (cpu.isActive()) {
			cpu.stop();
			return true;
		}
		cpu.setBoard(board);
		(new Thread(cpu)).start();
		return true;
	}

	public final void setStm()
	{
		String check, wstr, bstr;

		switch (board.isMate()) {
		case GenBoard.NOT_MATE:
		default:
			if (board.incheck(board.getStm()))
				check = " (check)";
			else
				check = "";
			if (runCPU())
				check = check + " thinking";
			break;
		case GenBoard.CHECK_MATE:
			check = " (checkmate)";
			break;
		case GenBoard.STALE_MATE:
			check = " (stalemate)";
			break;
		}
		if (type == Enums.LOCAL_GAME) {
			wstr = "White";
			bstr = "Black";
		} else {
			wstr = settings.getString("white");
			bstr = settings.getString("black");
		}

		final TextView white = (TextView) GenGame.self.findViewById(R.id.white_name);
		final TextView black = (TextView) GenGame.self.findViewById(R.id.black_name);
		if (board.getStm() == Piece.WHITE) {
			white.setText(wstr + check);
			black.setText(bstr);
			white.setTypeface(Typeface.DEFAULT_BOLD);
			black.setTypeface(Typeface.DEFAULT);
		} else {
			white.setText(wstr);
			black.setText(bstr + check);
			white.setTypeface(Typeface.DEFAULT);
			black.setTypeface(Typeface.DEFAULT_BOLD);
		}
	}

	public Bundle getBundle()
	{
		settings.putString("history", history.toString());
		return settings;
	}

	public void save(final Context context, final boolean exitgame)
	{
		switch (type) {
		case Enums.LOCAL_GAME:
			final GameDataDB db = new GameDataDB(context);
			final int id = Integer.valueOf(settings.getString("id"));

			if (history.size() < 1) {
				db.deleteLocalGame(id);
				db.close();
				return;
			}
			if (exitgame) {
				db.close();
				return;
			}
			final long stime = (new Date()).getTime();
			final String zfen = board.printZfen();
			final String hist = history.toString();

			db.saveLocalGame(id, stime, zfen, hist);
			db.close();
			break;
		case Enums.ONLINE_GAME:
			if (exitgame)
				return;
			GenGame.self.displaySubmitMove();
		case Enums.ARCHIVE_GAME:
			break;
		}
	}

	public void setCpuTime()
	{
		(new CpuTimeDialog(context, handle, cpu.getTime())).show();
	}

	public void resign()
	{
		(new ResignConfirm(context, handle)).show();
	}

	public void rematch()
	{
		final String opp = settings.getString("username").equals(settings.getString("white"))?
			settings.getString("black") : settings.getString("white");
		(new RematchConfirm(context, handle, opp)).show();
	}

	public void resync()
	{
		progress.setText("Updating Game State");
		net.game_status(settings.getString("gameid"));
		(new Thread(net)).start();
	}

	private void applyRemoteMove(final String hist)
	{
		if (hist == null || hist.length() < 3)
			return;

		final String[] movehistory = hist.trim().split(" +");
		if (movehistory[movehistory.length - 1].equals(history.top().toString()))
			return;

		// must be on most current move to apply it
		currentMove();
		Toast.makeText(context, "New move loaded...", Toast.LENGTH_LONG).show();

		final GenMove move = new GenMove();
		move.parse(movehistory[movehistory.length - 1]);
		if (board.validMove(move) != GenBoard.VALID_MOVE)
			return;
		applyMove(move, true, false);
	}

	public void reset()
	{
		hindex = -1;
		callstack.clear();
		history.clear();
		board.reset();
		GenGame.self.reset();
	}

	public void backMove()
	{
		if (hindex < 0)
			return;
		final GenMove move = history.get(hindex);
		revertMove(move);
	}

	public void forwardMove()
	{
		if (hindex + 1 >= history.size())
			return;
		final GenMove move = history.get(hindex + 1);
		applyMove(move, false, true);
	}

	public void currentMove()
	{
		while (hindex + 1 < history.size()) {
			final GenMove move = history.get(hindex + 1);
			applyMove(move, false, true);
		}
	}

	public void firstMove()
	{
		while (hindex > 0) {
			final GenMove move = history.get(hindex);
			revertMove(move);
		}
	}

	public void undoMove()
	{
		if (hindex < 0)
			return;
		final GenMove move = history.get(hindex);
		revertMove(move);
		history.pop();
	}

	public void submitMove()
	{
		progress.setText("Sending Move");

		final String gameid = settings.getString("gameid");
		final String move = history.top().toString();

		net.submit_move(gameid, move);
		(new Thread(net)).start();
	}

	private void handleMove()
	{
		switch (type) {
		case Enums.ONLINE_GAME:
			// you can't edit the past in online games
			if (hindex + 1 < history.size()) {
				callstack.pop();
				return;
			}
			break;
		case Enums.ARCHIVE_GAME:
			return;
		}

		final GenMove move = new GenMove();

		// create move
		if (callstack.get(0) > 64) {
			move.index = Math.abs(callstack.get(0) - 100);
			move.from = Piece.PLACEABLE;
		} else {
			move.from = callstack.get(0);
		}
		move.to = callstack.get(1);

		// return if move isn't valid
		if (board.validMove(move) != GenBoard.VALID_MOVE) {
			callstack.pop();
			return;
		}
		callstack.clear();
		applyMove(move, true, true);
	}

	private void applyMove(final GenMove move, final boolean erase, final boolean localmove)
	{
		if (hindex >= 0) {
			// undo last move highlight
			final BoardButton to = (BoardButton) GenGame.self.findViewById(history.get(hindex).to);
			to.setLast(false);

			if (hindex > 1) {
				// legal move always ends with king not in check
				final int king = board.kingIndex(board.getStm());
				final BoardButton kingI = (BoardButton) GenGame.self.findViewById(king);
				kingI.setCheck(false);
			}
		}

		if (move.from == Piece.PLACEABLE) {
			final PlaceButton from = (PlaceButton) GenGame.self.findViewById(GenBoard.pieceType[move.index] + 100);
			final BoardButton to = (BoardButton) GenGame.self.findViewById(move.to);

			from.setHighlight(false);
			from.minusPiece();
			to.setPiece(from.getPiece());
			to.setLast(true);
		} else {
			final BoardButton from = (BoardButton) GenGame.self.findViewById(move.from);
			final BoardButton to = (BoardButton) GenGame.self.findViewById(move.to);

			to.setPiece(from.getPiece());
			to.setLast(true);
			from.setPiece(0);
			from.setHighlight(false);
		}

		// apply move to board
		board.make(move);
		// update hindex, history
		hindex++;
		if (erase) {
			if (hindex < history.size())
				history.resize(hindex);
			history.push(move);
			if (localmove)
				save(GenGame.self.game_board.getContext(), false);
		}

		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) GenGame.self.findViewById(king);
			kingI.setCheck(true);
		}
		setStm();
	}

	private void revertMove(final GenMove move)
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) GenGame.self.findViewById(king);
			kingI.setCheck(false);
		}

		if (move.from == Piece.PLACEABLE) {
			final PlaceButton from = (PlaceButton) GenGame.self.findViewById(GenBoard.pieceType[move.index] + 100);
			final BoardButton to = (BoardButton) GenGame.self.findViewById(move.to);

			to.setLast(false);
			to.setPiece(0);
			from.plusPiece();
		} else {
			final BoardButton from = (BoardButton) GenGame.self.findViewById(move.from);
			final BoardButton to = (BoardButton) GenGame.self.findViewById(move.to);

			from.setPiece(to.getPiece());

			to.setLast(false);
			if (move.xindex == Piece.NONE)
				to.setPiece(0);
			else
				to.setPiece(GenBoard.pieceType[move.xindex]);
		}
		hindex--;
		board.unmake(move);

		if (hindex >= 0) {
			// redo last move highlight
			final BoardButton to = (BoardButton) GenGame.self.findViewById(history.get(hindex).to);
			to.setLast(true);
		}
		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) GenGame.self.findViewById(king);
			kingI.setCheck(true);
		}
		setStm();
	}

	public void boardClick(final View v)
	{
		final BoardButton to = (BoardButton) v;
		final int index = to.getIndex();
		final int col = yourColor();

		if (callstack.size() == 0) {
		// No active clicks
			// first click must be non empty and your own
			if (to.getPiece() * col <= 0)
				return;
			callstack.push(index);
			to.setHighlight(true);
			return;
		} else if (callstack.get(0) == index) {
		// clicking the same square
			callstack.clear();
			to.setHighlight(false);
			return;
		} else if (callstack.get(0) > 64) {
		// Place piece action
			// can't place on another piece
			if (to.getPiece() * col < 0) {
				return;
			} else if (to.getPiece() * col > 0) {
				final PlaceButton from = (PlaceButton) GenGame.self.findViewById(callstack.get(0));
				from.setHighlight(false);
				to.setHighlight(true);
				callstack.set(0, index);
				return;
			}
		} else {
		// piece move action
			final BoardButton from = (BoardButton) GenGame.self.findViewById(callstack.get(0));
			// capturing your own piece (switch to piece)
			if (from.getPiece() * to.getPiece() > 0) {
				from.setHighlight(false);
				to.setHighlight(true);
				callstack.set(0, index);
				return;
			}
		}
		callstack.push(index);
		handleMove();
	}

	public void placeClick(final View v)
	{
		final PlaceButton from = (PlaceButton) v;
		final int col = yourColor();
		final int ptype = from.getPiece();

		// only select your own pieces where count > 0
		if (board.getStm() != col || ptype * board.getStm() < 0 || from.getCount() <= 0)
			return;
		if (callstack.size() == 0) {
		// No active clicks
			callstack.push(ptype + 100);
			from.setHighlight(true);
		} else if (callstack.get(0) < 64) {
		// switching from board to place piece
			final BoardButton to = (BoardButton) GenGame.self.findViewById(callstack.get(0));
			to.setHighlight(false);
			callstack.set(0, ptype + 100);
			from.setHighlight(true);
		} else if (callstack.get(0) == ptype + 100) {
		// clicking the same square
			callstack.clear();
			from.setHighlight(false);
		} else {
		// switching to another place piece
			final PlaceButton fromold = (PlaceButton) GenGame.self.findViewById(callstack.get(0));
			fromold.setHighlight(false);
			callstack.set(0, ptype + 100);
			from.setHighlight(true);
		}
		GenGame.self.game_board.flip();
	}
}
