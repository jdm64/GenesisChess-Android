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
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;

public class RegGameState extends GameState
{
	private final ObjectArray<MoveFlags> flagsHistory;

	private final Handler xhandle = new Handler()
	{
		@Override
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case PawnPromoteDialog.MSG:
				callstack.clear();
				applyMove((RegMove) msg.obj, true, true);
				break;
			default:
				handleOther(msg);
				break;
			}
		}
	};

	public RegGameState(final Activity _activity, final GameFrag _game, final Bundle _settings)
	{
		super(_activity, _game, _settings);
		handle = xhandle;
		moveType = new RegMove();
		board = new RegBoard();
		flagsHistory = new ObjectArray<MoveFlags>();

		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
			cpu = new RegEngine(handle);
			cpu.setTime(pref.getInt(PrefKey.CPUTIME, cpu.getTime()));
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

		for (final String element : movehistory) {
			final RegMove move = new RegMove();
			move.parse(element);

			if (board.validMove(move) != Move.VALID_MOVE)
				break;
			final MoveFlags flags = new MoveFlags();
			board.getMoveFlags(flags);
			flagsHistory.push(flags);
			history.push(move);
			board.make(move);
			hindex++;
		}
		check_endgame();
	}

	@Override
	public void setBoard()
	{
		// set dead piece counts
		setBoard(board.getPieceCounts(Piece.DEAD));
	}

	@Override
	public void reset()
	{
		super.reset();
		flagsHistory.clear();
	}

	@Override
	public void undoMove()
	{
		super.undoMove();
		flagsHistory.pop();
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
		if (board.validMove(move) != Move.VALID_MOVE) {
			callstack.pop();
			return;
		} else if (move.getPromote() != 0) {
			new PawnPromoteDialog(activity, handle, move, board.getStm()).show();

			callstack.pop();
			return;
		}
		callstack.clear();
		applyMove(move, true, true);
	}

	@Override
	protected void applyMove(final Move move, final boolean erase, final boolean localmove)
	{
		preApplyMove();

		final BoardButton from = (BoardButton) activity.findViewById(move.from);
		final BoardButton to = (BoardButton) activity.findViewById(move.to);

		to.setPiece(from.getPiece());
		to.setLast(true);
		from.setPiece(0);
		from.setHighlight(false);

		if (move.xindex != Piece.NONE) {
			final PlaceButton piece = (PlaceButton) activity.findViewById(board.PieceType(move.xindex) + 1000);
			piece.plusCount();
		}

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == Move.CASTLE_QS);
			final int castleTo = move.to + (left? 1 : -1),
				castleFrom = (left? 0:7) + ((board.getStm() == Piece.WHITE)? Piece.A1 : Piece.A8);

			BoardButton castle = (BoardButton) activity.findViewById(castleFrom);
			castle.setPiece(Piece.EMPTY);
			castle = (BoardButton) activity.findViewById(castleTo);
			castle.setPiece(Piece.ROOK * board.getStm());
		} else if (move.getPromote() != 0) {
			final BoardButton pawn = (BoardButton) activity.findViewById(move.to);
			pawn.setPiece(move.getPromote() * board.getStm());
		} else if (move.getEnPassant()) {
			final BoardButton pawn = (BoardButton) activity.findViewById(board.Piece(move.xindex));
			pawn.setPiece(Piece.EMPTY);
		}
		// get copy of board flags
		final MoveFlags flags = new MoveFlags();
		board.getMoveFlags(flags);

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
		postApplyMove();
	}

	@Override
	protected void revertMove(final Move move)
	{
		preRevertMove();

		final BoardButton from = (BoardButton) activity.findViewById(move.from);
		final BoardButton to = (BoardButton) activity.findViewById(move.to);

		from.setPiece(to.getPiece());
		to.setLast(false);

		if (move.xindex == Piece.NONE) {
			to.setPiece(Piece.EMPTY);
		} else if (move.getEnPassant()) {
			final int loc = move.to + ((move.to - move.from > 0)? -16 : 16);
			final BoardButton pawn = (BoardButton) activity.findViewById(loc);
			pawn.setPiece(Piece.PAWN * board.getStm());
			to.setPiece(Piece.EMPTY);
		} else {
			to.setPiece(board.PieceType(move.xindex));
		}

		if (move.xindex != Piece.NONE) {
			final PlaceButton piece = (PlaceButton) activity.findViewById(board.PieceType(move.xindex) + 1000);
			piece.minusCount();
		}

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == Move.CASTLE_QS);
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

		postRevertMove();
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
		// Required because GameState calls this function
	}
}
