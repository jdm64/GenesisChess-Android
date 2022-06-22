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
	public final static int PLACEOFFSET = 1000;

	public GenGameModel(IGameView2 _view, IGameController2 controller)
	{
		super(new GenBoard(), _view, controller);
	}

	@Override
	public void handleMove(int from, int to)
	{
		var move = board.newMove();

		// create move
		if (from > 0x88) {
			move.index = Math.abs(from - PLACEOFFSET);
			move.from = Piece.PLACEABLE;
		} else {
			move.from = from;
		}
		move.to = to;

		// return if move isn't valid
		if (board.validMove(move) != Move.VALID_MOVE) {
			return;
		}
		applyMove(move, true);
	}

	@Override
	public void applyMove(Move move, boolean erase)
	{
		preApplyMove();

		if (move.from == Piece.PLACEABLE) {
			var to = view.getBoardSq(move.to);

			to.setPiece(Move.InitPieceType[move.index]);
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
			var to = view.getBoardSq(move.to);

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
				to.setPiece(Move.InitPieceType[move.xindex]);
		}

		board.unmake(move);
		hindex--;

		postRevertMove();
	}
}