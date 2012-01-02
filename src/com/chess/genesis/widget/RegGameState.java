package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

class RegGameState extends GameState
{
	private final ObjectArray<MoveFlags> flagsHistory;
	private final ObjectArray<RegMove> history;
	private final RegBoard board;

	private final Handler xhandle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case RegEngine.MSG:
				final Bundle bundle = (Bundle) msg.obj;

				if (bundle.getLong("time") == 0) {
					cpu.setBoard(board);
					(new Thread(cpu)).start();
					return;
				}
				final RegMove move = bundle.getParcelable("move");

				currentMove();
				applyMove(move, true, true);
				break;
			default:
				handleOther(msg);
				break;
			}
		}
	};

	public RegGameState(final Game _activity, final Bundle _settings)
	{
		activity = _activity;
		settings = _settings;
		handle = xhandle;

		callstack = new IntArray();
		flagsHistory = new ObjectArray<MoveFlags>();
		history = new ObjectArray<RegMove>();
		board = new RegBoard();
		progress = new ProgressMsg(activity);

		type = settings.getInt("type", Enums.ONLINE_GAME);
		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
			cpu = new RegEngine(handle);
			cpu.setTime(pref.getInt("cputime", cpu.getTime()));
			oppType = Integer.valueOf(settings.getString("opponent"));
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

		final String tmp = settings.getString("history");
		if (tmp == null || tmp.length() < 3) {
			check_endgame();
			return;
		}
		final String[] movehistory = tmp.trim().split(" +");

		for (int i = 0; i < movehistory.length; i++) {
			final RegMove move = new RegMove();
			move.parse(movehistory[i]);

			if (board.validMove(move) != RegBoard.VALID_MOVE)
				break;
			flagsHistory.push(board.getMoveFlags());
			history.push(move);
			board.make(move);
			hindex++;
		}
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

	public void setBoard()
	{
		// set place piece counts
		final int[] pieces = board.getPieceCounts(Piece.DEAD);
		for (int i = -6; i < 0; i++) {
			final PlaceButton button = (PlaceButton) activity.findViewById(i + 100);
			button.setCount(pieces[i + 6]);
		}
		for (int i = 1; i < 7; i++) {
			final PlaceButton button = (PlaceButton) activity.findViewById(i + 100);
			button.setCount(pieces[i + 6]);
		}

		// set board pieces
		final int[] squares = board.getBoardArray();
		for (int i = 0; i < 64; i++) {
			final BoardButton button = (BoardButton) activity.findViewById(i);
			button.setPiece(squares[i]);
		}
		// set last move highlight
		if (history.size() != 0) {
			final BoardButton to = (BoardButton) activity.findViewById(history.top().to);
			to.setLast(true);
		}

		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) activity.findViewById(king);
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
		String think = "", wstr, bstr;
		boolean mate = false;

		switch (board.isMate()) {
		case RegBoard.NOT_MATE:
		default:
			if (runCPU())
				think = " thinking";
			break;
		case RegBoard.CHECK_MATE:
		case RegBoard.STALE_MATE:
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
				white.setTabTextColor(0xffeb0000);
		} else {
			white.setText(wstr);
			white.setActive(false);

			black.setText(bstr + think);
			black.setActive(true);

			if (mate)
				black.setTabTextColor(0xffeb0000);
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
			activity.displaySubmitMove();
		case Enums.ARCHIVE_GAME:
			break;
		}
	}

	protected void applyRemoteMove(final String hist)
	{
		if (hist == null || hist.length() < 3)
			return;

		final String[] movehistory = hist.trim().split(" +");
		if (movehistory[movehistory.length - 1].equals(history.top().toString()))
			return;

		// must be on most current move to apply it
		currentMove();
		Toast.makeText(activity, "New move loaded...", Toast.LENGTH_LONG).show();

		final RegMove move = new RegMove();
		move.parse(movehistory[movehistory.length - 1]);
		if (board.validMove(move) != RegBoard.VALID_MOVE)
			return;
		applyMove(move, true, false);
	}

	public void reset()
	{
		super.reset();

		history.clear();
		flagsHistory.clear();
		board.reset();
	}

	public void backMove()
	{
		if (hindex < 0)
			return;
		final RegMove move = history.get(hindex);
		revertMove(move);
	}

	public void forwardMove()
	{
		if (hindex + 1 >= history.size())
			return;
		final RegMove move = history.get(hindex + 1);
		applyMove(move, false, true);
	}

	public void currentMove()
	{
		while (hindex + 1 < history.size()) {
			final RegMove move = history.get(hindex + 1);
			applyMove(move, false, true);
		}
	}

	public void firstMove()
	{
		while (hindex > 0) {
			final RegMove move = history.get(hindex);
			revertMove(move);
		}
	}

	public void undoMove()
	{
		if (hindex < 0)
			return;
		final RegMove move = history.get(hindex);
		revertMove(move);
		history.pop();
		flagsHistory.pop();
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
		if (type == Enums.ONLINE_GAME) {
			// you can't edit the past in online games
			if (hindex + 1 < history.size()) {
				callstack.pop();
				return;
			}
		} else if (type == Enums.ARCHIVE_GAME) {
			return;
		}

		final RegMove move = new RegMove();

		move.from = callstack.get(0);
		move.to = callstack.get(1);

		// return if move isn't valid
		if (board.validMove(move) != RegBoard.VALID_MOVE) {
			callstack.pop();
			return;
		}
		callstack.clear();
		applyMove(move, true, true);
	}

	private void applyMove(final RegMove move, final boolean erase, final boolean localmove)
	{
		if (hindex >= 0) {
			// undo last move highlight
			final BoardButton to = (BoardButton) activity.findViewById(history.get(hindex).to);
			to.setLast(false);

			if (hindex > 1) {
				// legal move always ends with king not in check
				final int king = board.kingIndex(board.getStm());
				final BoardButton kingI = (BoardButton) activity.findViewById(king);
				kingI.setCheck(false);
			}
		}

		final BoardButton from = (BoardButton) activity.findViewById(move.from);
		final BoardButton to = (BoardButton) activity.findViewById(move.to);

		to.setPiece(from.getPiece());
		to.setLast(true);
		from.setPiece(0);
		from.setHighlight(false);

		if (move.xindex != Piece.NONE) {
			final PlaceButton piece = (PlaceButton) activity.findViewById(board.piece[move.xindex].type + 100);
			piece.plusPiece();
		}

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == 0x20);
			final int castleTo = move.to + (left? 1 : -1),
				castleFrom = (left? 0:7) + ((board.getStm() == Piece.WHITE)? Piece.A1 : Piece.A8);

			BoardButton castle = (BoardButton) activity.findViewById(castleFrom);
			castle.setPiece(Piece.EMPTY);
			castle = (BoardButton) activity.findViewById(castleTo);
			castle.setPiece(Piece.ROOK * board.getStm());
		} else if (move.getPromote() != 0) {
			final BoardButton pawn = (BoardButton) activity.findViewById(move.to);
			pawn.setPiece(Piece.QUEEN * board.getStm());
		} else if (move.getEnPassant()) {
			final BoardButton pawn = (BoardButton) activity.findViewById(board.piece[move.xindex].loc);
			pawn.setPiece(Piece.EMPTY);
		}
		// get copy of board flags
		final MoveFlags flags = board.getMoveFlags();

		// apply move to board
		board.make(move);

		// update hindex, history
		hindex++;
		if (erase) {
			if (hindex < history.size()) {
				history.resize(hindex);
				flagsHistory.resize(hindex);
			}
			history.push(move);
			flagsHistory.push(flags);
			if (localmove)
				save(activity, false);
		}

		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) activity.findViewById(king);
			kingI.setCheck(true);
		}
		setStm();
	}

	private void revertMove(final RegMove move)
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) activity.findViewById(king);
			kingI.setCheck(false);
		}

		final BoardButton from = (BoardButton) activity.findViewById(move.from);
		final BoardButton to = (BoardButton) activity.findViewById(move.to);

		from.setPiece(to.getPiece());
		to.setLast(false);

		if (move.xindex == Piece.NONE) {
			to.setPiece(Piece.EMPTY);
		} else if (move.getEnPassant()) {
			final int loc = move.to + ((move.from - move.to > 0)? 8 : -8);
			final BoardButton pawn = (BoardButton) activity.findViewById(loc);
			pawn.setPiece(Piece.PAWN * board.getStm());
			to.setPiece(Piece.EMPTY);
		} else {
			to.setPiece(board.piece[move.xindex].type);
		}

		if (move.xindex != Piece.NONE) {
			final PlaceButton piece = (PlaceButton) activity.findViewById(board.piece[move.xindex].type + 100);
			piece.minusPiece();
		}

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == 0x20);
			final int castleTo = move.to + (left? 1 : -1),
				castleFrom = (left? 0:7) + ((board.getStm() == Piece.BLACK)? Piece.A1 : Piece.A8);
			
			BoardButton castle = (BoardButton) activity.findViewById(castleFrom);
			castle.setPiece(Piece.ROOK * -board.getStm());
			castle = (BoardButton) activity.findViewById(castleTo);
			castle.setPiece(Piece.EMPTY);
		} else if (move.getPromote() != 0) {
			final BoardButton pawn = (BoardButton) activity.findViewById(move.from);
			pawn.setPiece(Piece.PAWN * -board.getStm());
		}

		board.unmake(move, flagsHistory.get(hindex));
		hindex--;

		if (hindex >= 0) {
			// redo last move highlight
			final BoardButton hto = (BoardButton) activity.findViewById(history.get(hindex).to);
			hto.setLast(true);
		}
		// move caused check
		if (board.incheck(board.getStm())) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) activity.findViewById(king);
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
		} else {
		// piece move action
			final BoardButton from = (BoardButton) activity.findViewById(callstack.get(0));
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
		// Required because GameState calls this function
	}
}
