/*	GenesisChess, an Android chess application
	Copyright (C) 2012, Justin Madru (justin.jdm64@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.chess.genesis.engine;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;

public class GenGameState extends GameState
{
	private final Handler xhandle = new Handler()
	{
		@Override
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case GenEngine.MSG:
				final Bundle bundle = (Bundle) msg.obj;

				if (bundle.getLong("time") == 0) {
					cpu.setBoard((GenBoard) board);
					new Thread(cpu).start();
					return;
				} else if (activity.isFinishing()) {
					// activity is gone, so give up!
					return;
				}
				currentMove();

				final GenMove tmove = bundle.getParcelable("move");
				final GenMove move = new GenMove();
				if (board.validMove(tmove, move))
					applyMove(move, true, true);
				break;
			default:
				handleOther(msg);
				break;
			}
		}
	};

	public GenGameState(final Activity _activity, final GameFrag _game, final Bundle _settings)
	{
		activity = _activity;
		game = _game;
		settings = _settings;
		handle = xhandle;

		callstack = new IntArray();
		history = new ObjectArray<Move>();
		board = new GenBoard();
		progress = new ProgressMsg(activity);

		type = settings.getInt("type", Enums.ONLINE_GAME);
		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
			cpu = new GenEngine(handle);
			cpu.setTime(pref.getInt("cputime", cpu.getTime()));
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

		final String tmp = settings.getString("history");
		if (tmp == null || tmp.length() < 3) {
			check_endgame();
			return;
		}
		final String[] movehistory = tmp.trim().split(" +");

		for (int i = 0; i < movehistory.length; i++) {
			final GenMove move = new GenMove();
			move.parse(movehistory[i]);

			if (board.validMove(move) != Move.VALID_MOVE)
				break;
			history.push(move);
			board.make(move);
			hindex++;
		}
		check_endgame();
	}

	@Override
	public void setBoard()
	{
		// set place piece counts
		setBoard(board.getPieceCounts(Piece.PLACEABLE));
	}

	@Override
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
		cpu.setBoard((GenBoard) board);
		new Thread(cpu).start();
		return true;
	}

	@Override
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

		final GenMove move = new GenMove();
		move.parse(sMove);
		if (board.validMove(move) != Move.VALID_MOVE)
			return;
		applyMove(move, true, false);
	}

	@Override
	public void reset()
	{
		super.reset();

		history.clear();
		board.reset();
	}

	@Override
	public void backMove()
	{
		if (hindex < 0)
			return;
		final GenMove move = (GenMove) history.get(hindex);
		revertMove(move);
	}

	@Override
	public void forwardMove()
	{
		if (hindex + 1 >= history.size())
			return;
		final GenMove move = (GenMove) history.get(hindex + 1);
		applyMove(move, false, true);
	}

	@Override
	public void currentMove()
	{
		while (hindex + 1 < history.size()) {
			final GenMove move = (GenMove) history.get(hindex + 1);
			applyMove(move, false, true);
		}
	}

	@Override
	public void firstMove()
	{
		while (hindex > 0) {
			final GenMove move = (GenMove) history.get(hindex);
			revertMove(move);
		}
	}

	@Override
	public void undoMove()
	{
		if (hindex < 0)
			return;
		final GenMove move = (GenMove) history.get(hindex);
		revertMove(move);
		history.pop();
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

		final GenMove move = new GenMove();

		// create move
		if (callstack.get(0) > 0x88) {
			move.index = Math.abs(callstack.get(0) - 1000);
			move.from = Piece.PLACEABLE;
		} else {
			move.from = callstack.get(0);
		}
		move.to = callstack.get(1);

		// return if move isn't valid
		if (board.validMove(move) != Move.VALID_MOVE) {
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
			final BoardButton to = (BoardButton) activity.findViewById(history.get(hindex).to);
			to.setLast(false);

			if (hindex > 1) {
				// legal move always ends with king not in check
				final int king = board.kingIndex(board.getStm());
				final BoardButton kingI = (BoardButton) activity.findViewById(king);
				kingI.setCheck(false);
			}
		}

		if (move.from == Piece.PLACEABLE) {
			final PlaceButton from = (PlaceButton) activity.findViewById(Move.InitPieceType[move.index] + 1000);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			from.setHighlight(false);
			from.minusPiece();
			to.setPiece(from.getPiece());
			to.setLast(true);
		} else {
			final BoardButton from = (BoardButton) activity.findViewById(move.from);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

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

	private void revertMove(final GenMove move)
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			final int king = board.kingIndex(board.getStm());
			final BoardButton kingI = (BoardButton) activity.findViewById(king);
			kingI.setCheck(false);
		}

		if (move.from == Piece.PLACEABLE) {
			final PlaceButton from = (PlaceButton) activity.findViewById(Move.InitPieceType[move.index] + 1000);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			to.setLast(false);
			to.setPiece(0);
			from.plusPiece();
		} else {
			final BoardButton from = (BoardButton) activity.findViewById(move.from);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			from.setPiece(to.getPiece());

			to.setLast(false);
			if (move.xindex == Piece.NONE)
				to.setPiece(0);
			else
				to.setPiece(Move.InitPieceType[move.xindex]);
		}

		board.unmake(move);
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

	@Override
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
		} else if (callstack.get(0) > 0x88) {
		// Place piece action
			// can't place on another piece
			if (to.getPiece() * col < 0) {
				return;
			} else if (to.getPiece() * col > 0) {
				final PlaceButton from = (PlaceButton) activity.findViewById(callstack.get(0));
				from.setHighlight(false);
				to.setHighlight(true);
				callstack.set(0, index);
				return;
			}
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

	@Override
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
			callstack.push(ptype + 1000);
			from.setHighlight(true);
		} else if (callstack.get(0) < 0x88) {
		// switching from board to place piece
			final BoardButton to = (BoardButton) activity.findViewById(callstack.get(0));
			to.setHighlight(false);
			callstack.set(0, ptype + 1000);
			from.setHighlight(true);
		} else if (callstack.get(0) == ptype + 1000) {
		// clicking the same square
			callstack.clear();
			from.setHighlight(false);
		} else {
		// switching to another place piece
			final PlaceButton fromold = (PlaceButton) activity.findViewById(callstack.get(0));
			fromold.setHighlight(false);
			callstack.set(0, ptype + 1000);
			from.setHighlight(true);
		}
		game.game_board.flip();
	}
}