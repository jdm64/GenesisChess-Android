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

import com.chess.genesis.activity.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.util.*;

public class RegGameState extends GameState
{
	private final ObjectArray<MoveFlags> flagsHistory;

	public RegGameState(final GameFrag _game)
	{
		super(_game.getActivity(), _game, new RegBoard());
		flagsHistory = new ObjectArray<>(new MoveFlags());

		final String tmp = settings.getString("history");
		if (tmp == null || tmp.length() < 3) {
			check_endgame();
			return;
		}
		final String[] movehistory = tmp.trim().split(" +");

		for (final String element : movehistory) {
			final Move move = board.newMove();
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
	protected void resetPieces()
	{
		super.resetPieces();

		for (int i = 0; i < 64; i++)
			gamefrag.getBoardSq(i).setPiece(Piece.EMPTY);
		for (int i = 0; i < 32; i++)
			gamefrag.getBoardSq(BaseBoard.EE64(RegBoard.InitRegPiece[i])).setPiece(Move.InitPieceType[i]);
	}

	@Override
	public void undoMove()
	{
		super.undoMove();
		flagsHistory.pop();
	}

	@Override
	public void handleMove(final int from, final int to)
	{
		if (boardNotEditable())
			return;

		final Move move = board.newMove();
		move.from = from;
		move.to = to;

		// return if move isn't valid
		if (board.validMove(move) != Move.VALID_MOVE) {
			return;
		} else if (move.getPromote() != 0) {
			new PawnPromoteDialog(activity, handle, move, board.getStm()).show();
			return;
		}
		applyMove(move, true, true);
	}

	@Override
	protected void applyMove(final Move move, final boolean erase, final boolean localmove)
	{
		preApplyMove();

		final IBoardSq from = gamefrag.getBoardSq(move.from);
		final IBoardSq to = gamefrag.getBoardSq(move.to);

		to.setPiece(from.getPiece());
		to.setLast(true);
		from.setPiece(0);

		if (move.xindex != Piece.NONE) {
			final IPlaceSq piece = gamefrag.getPlaceSq(board.PieceType(move.xindex) + PLACEOFFSET);
			piece.plusCount();
		}

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == Move.CASTLE_QS);
			final int castleTo = move.to + (left? 1 : -1),
				castleFrom = (left? 0:7) + ((board.getStm() == Piece.WHITE)? Piece.A1 : Piece.A8);

			IBoardSq castle = gamefrag.getBoardSq(castleFrom);
			castle.setPiece(Piece.EMPTY);
			castle = gamefrag.getBoardSq(castleTo);
			castle.setPiece(Piece.ROOK * board.getStm());
		} else if (move.getPromote() != 0) {
			final IBoardSq pawn = gamefrag.getBoardSq(move.to);
			pawn.setPiece(move.getPromote() * board.getStm());
		} else if (move.getEnPassant()) {
			final IBoardSq pawn = gamefrag.getBoardSq(board.Piece(move.xindex));
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

		final IBoardSq from = gamefrag.getBoardSq(move.from);
		final IBoardSq to = gamefrag.getBoardSq(move.to);

		from.setPiece(to.getPiece());
		to.setLast(false);

		if (move.xindex == Piece.NONE) {
			to.setPiece(Piece.EMPTY);
		} else if (move.getEnPassant()) {
			final int loc = move.to + ((move.to - move.from > 0)? -16 : 16);
			final IBoardSq pawn = gamefrag.getBoardSq(loc);
			pawn.setPiece(Piece.PAWN * board.getStm());
			to.setPiece(Piece.EMPTY);
		} else {
			to.setPiece(board.PieceType(move.xindex));
		}

		if (move.xindex != Piece.NONE) {
			final IPlaceSq piece = gamefrag.getPlaceSq(board.PieceType(move.xindex) + PLACEOFFSET);
			piece.minusCount();
		}

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == Move.CASTLE_QS);
			final int castleTo = move.to + (left? 1 : -1),
				castleFrom = (left? 0:7) + ((board.getStm() == Piece.BLACK)? Piece.A1 : Piece.A8);

			IBoardSq castle = gamefrag.getBoardSq(castleFrom);
			castle.setPiece(Piece.ROOK * -board.getStm());
			castle = gamefrag.getBoardSq(castleTo);
			castle.setPiece(Piece.EMPTY);
		} else if (move.getPromote() != 0) {
			final IBoardSq pawn = gamefrag.getBoardSq(move.from);
			pawn.setPiece(Piece.PAWN * -board.getStm());
		}

		board.unmake(move, flagsHistory.get(hindex));
		hindex--;

		postRevertMove();
	}

	@Override
	public void boardClick(final IBoardSq sq)
	{
		hintList.boardClick(sq, yourColor());
	}

	@Override
	public void placeClick(final IPlaceSq sq)
	{
		// Required because GameState calls this function
	}
}
