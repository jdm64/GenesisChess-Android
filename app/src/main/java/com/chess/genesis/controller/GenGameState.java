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

import com.chess.genesis.activity.*;
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;

public class GenGameState extends GameState
{
	public GenGameState(final GameFrag _game)
	{
		super(_game.getActivity(), _game, new GenBoard());

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
			history.push(move);
			board.make(move);
			hindex++;
		}
		check_endgame();
	}

	@Override
	protected void resetPieces()
	{
		super.resetPieces();

		for (int i = 0; i < 64; i++)
			gamefrag.getBoardSq(i).reset();
	}

	@Override
	public void loadBoard()
	{
		// set place piece counts
		setBoard(board.getPieceCounts(Piece.PLACEABLE));
	}

	@Override
	public void handleMove(final int from, final int to)
	{
		if (boardNotEditable())
			return;

		final Move move = board.newMove();

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
		applyMove(move, true, true);
	}

	@Override
	protected void applyMove(final Move move, final boolean erase, final boolean localmove)
	{
		preApplyMove();

		if (move.from == Piece.PLACEABLE) {
			final IPlaceSq from = gamefrag.getPlaceSq(Move.InitPieceType[move.index] + PLACEOFFSET);
			final IBoardSq to = gamefrag.getBoardSq(move.to);

			from.minusCount();
			to.setPiece(from.getPiece());
			to.setLast(true);
		} else {
			final IBoardSq from = gamefrag.getBoardSq(move.from);
			final IBoardSq to = gamefrag.getBoardSq(move.to);

			to.setPiece(from.getPiece());
			to.setLast(true);
			from.setPiece(Piece.EMPTY);
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
		postApplyMove();
	}

	@Override
	protected void revertMove(final Move move)
	{
		preRevertMove();

		if (move.from == Piece.PLACEABLE) {
			final IPlaceSq from = gamefrag.getPlaceSq(Move.InitPieceType[move.index] + PLACEOFFSET);
			final IBoardSq to = gamefrag.getBoardSq(move.to);

			to.setLast(false);
			to.setPiece(Piece.EMPTY);
			from.plusCount();
		} else {
			final IBoardSq from = gamefrag.getBoardSq(move.from);
			final IBoardSq to = gamefrag.getBoardSq(move.to);

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

	@Override
	public void onBoardClick(IBoardSq sq)
	{
		hintList.onBoardClick(sq, yourColor());
	}

	@Override
	public void onPlaceClick(IPlaceSq sq)
	{
		hintList.onPlaceClick(sq, yourColor());
	}
}
