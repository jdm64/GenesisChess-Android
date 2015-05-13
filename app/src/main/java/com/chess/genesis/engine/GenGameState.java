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

import android.view.*;
import com.chess.genesis.activity.*;

public class GenGameState extends GameState
{
	public GenGameState(final GameFrag _game)
	{
		super(_game, new GenBoard());

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
	public void setBoard()
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
			move.index = Math.abs(from - 1000);
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
			final IPlaceSq from = (IPlaceSq) activity.findViewById(Move.InitPieceType[move.index] + 1000);
			final IBoardSq to = (IBoardSq) activity.findViewById(move.to);

			from.minusCount();
			to.setPiece(from.getPiece());
			to.setLast(true);
		} else {
			final IBoardSq from = (IBoardSq) activity.findViewById(move.from);
			final IBoardSq to = (IBoardSq) activity.findViewById(move.to);

			to.setPiece(from.getPiece());
			to.setLast(true);
			from.setPiece(0);
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
			final IPlaceSq from = (IPlaceSq) activity.findViewById(Move.InitPieceType[move.index] + 1000);
			final IBoardSq to = (IBoardSq) activity.findViewById(move.to);

			to.setLast(false);
			to.setPiece(0);
			from.plusCount();
		} else {
			final IBoardSq from = (IBoardSq) activity.findViewById(move.from);
			final IBoardSq to = (IBoardSq) activity.findViewById(move.to);

			from.setPiece(to.getPiece());

			to.setLast(false);
			if (move.xindex == Piece.NONE)
				to.setPiece(0);
			else
				to.setPiece(Move.InitPieceType[move.xindex]);
		}

		board.unmake(move);
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
		hintList.placeClick(sq, yourColor());
	}
}
