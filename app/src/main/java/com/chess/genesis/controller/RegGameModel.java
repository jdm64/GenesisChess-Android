/* GenChess, a genesis chess engine
 * Copyright (C) 2022, Justin Madru (justin.jdm64@gmail.com)
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

import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;

public class RegGameModel extends GameModel
{
	public RegGameModel(IGameView _view, IGameController controller)
	{
		super(new RegBoard(), _view, controller);
	}

	@Override
	public void handleMove(int from, int to)
	{
		var move = board.parseMove(from, to);
		if (move == null) {
			return;
		} else if (move.getPromote() != 0) {
			view.showPromoteDialog(move, board.getStm());
			return;
		}
		applyMove(move, true);
	}

	@Override
	public void applyMove(Move move, boolean erase)
	{
		preApplyMove();

		var from = view.getBoardSq(move.from);
		var to = view.getBoardSq(move.to);

		to.setPiece(from.getPiece());
		to.setLast(true);
		from.setPiece(0);

		if (move.getCastle() != 0) {
			var left = (move.getCastle() == Move.CASTLE_QS);
			int castleTo = move.to + (left? 1 : -1),
			    castleFrom = (left? 0:7) + ((board.getStm() == Piece.WHITE)? Piece.A1 : Piece.A8);

			var castle = view.getBoardSq(castleFrom);
			castle.setPiece(Piece.EMPTY);
			castle = view.getBoardSq(castleTo);
			castle.setPiece(Piece.ROOK * board.getStm());
		} else if (move.getPromote() != 0) {
			var pawn = view.getBoardSq(move.to);
			pawn.setPiece(move.getPromote() * board.getStm());
		} else if (move.getEnPassant()) {
			var pawn = view.getBoardSq(board.pieceLoc(move.xindex));
			pawn.setPiece(Piece.EMPTY);
		}

		var flags = new MoveFlags();
		board.getMoveFlags(flags);
		board.make(move);
		hindex++;

		postCommonMove(erase);

		if (erase) {
			if (hindex < history.size()) {
				history.resize(hindex);
				flagsHistory.resize(hindex);
			}
			history.push(move);
			flagsHistory.push(flags);
			controller.onMove(move);
		}
	}

	@Override
	public void revertMove(Move move)
	{
		preRevertMove();

		var from = view.getBoardSq(move.from);
		var to = view.getBoardSq(move.to);

		from.setPiece(to.getPiece());
		to.setLast(false);

		if (move.xindex == Piece.NONE) {
			to.setPiece(Piece.EMPTY);
		} else if (move.getEnPassant()) {
			var loc = move.to + ((move.to - move.from > 0)? -16 : 16);
			var pawn = view.getBoardSq(loc);
			pawn.setPiece(Piece.PAWN * board.getStm());
			to.setPiece(Piece.EMPTY);
		} else {
			to.setPiece(board.pieceType(move.xindex));
		}

		if (move.getCastle() != 0) {
			var left = (move.getCastle() == Move.CASTLE_QS);
			int castleTo = move.to + (left? 1 : -1),
			    castleFrom = (left? 0:7) + ((board.getStm() == Piece.BLACK)? Piece.A1 : Piece.A8);

			var castle = view.getBoardSq(castleFrom);
			castle.setPiece(Piece.ROOK * -board.getStm());
			castle = view.getBoardSq(castleTo);
			castle.setPiece(Piece.EMPTY);
		} else if (move.getPromote() != 0) {
			var pawn = view.getBoardSq(move.from);
			pawn.setPiece(Piece.PAWN * -board.getStm());
		}

		board.unmake(move, flagsHistory.get(hindex));
		hindex--;

		postRevertMove();
	}
}
