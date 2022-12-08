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

public class GenGameModel extends GameModel
{
	public GenGameModel(IGameView _view, IGameController controller)
	{
		super(new GenBoard(), _view, controller);
	}

	@Override
	public void loadBoard()
	{
		super.loadBoard();
		view.setPlaceCounts(board.getPieceCounts(Piece.PLACEABLE));
	}

	@Override
	public void handleMove(int from, int to)
	{
		var move = board.parseMove(from, to);
		if (move == null)
			return;
		applyMove(move, true);
	}

	@Override
	public void applyMove(Move move, boolean erase)
	{
		preApplyMove();

		if (move.from == Piece.PLACEABLE) {
			var type = Board.InitPieceType[move.index];
			var from = view.getPlaceSq(type + Board.PLACEOFFSET);
			var to = view.getBoardSq(move.to);

			from.minusCount();
			to.setPiece(type);
			to.setLast(true);
		} else {
			var from = view.getBoardSq(move.from);
			var to = view.getBoardSq(move.to);

			to.setPiece(from.getPiece());
			to.setLast(true);
			from.setPiece(Piece.EMPTY);
		}

		board.make(move);
		hindex++;

		postCommonMove(erase);

		if (erase) {
			if (hindex < history.size())
				history.resize(hindex);
			history.push(move);
			controller.onMove(move);
		}
	}

	@Override
	public void revertMove(Move move)
	{
		preRevertMove();

		if (move.from == Piece.PLACEABLE) {
			var type = Board.InitPieceType[move.index];
			var from = view.getPlaceSq(type + Board.PLACEOFFSET);
			var to = view.getBoardSq(move.to);

			from.plusCount();
			to.setLast(false);
			to.setPiece(Piece.EMPTY);
		} else {
			var from = view.getBoardSq(move.from);
			var to = view.getBoardSq(move.to);

			from.setPiece(to.getPiece());

			to.setLast(false);
			if (move.xindex == Piece.NONE)
				to.setPiece(Piece.EMPTY);
			else
				to.setPiece(Board.InitPieceType[move.xindex]);
		}

		board.unmake(move, flagsHistory.get(hindex));
		hindex--;

		postRevertMove();
	}
}
