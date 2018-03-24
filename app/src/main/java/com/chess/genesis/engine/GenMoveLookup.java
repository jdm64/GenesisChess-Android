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

abstract class GenMoveLookup extends BaseBoard
{
	private final int[] list = new int[28];

	int[] genAll(final int From)
	{
		final int type = Math.abs(square[From]);
		final int[] offset = offsets[type];
		int next = 0;

		if (type == Piece.PAWN) {
			boolean evn = true;
			for (int i = 0; offset[i] != 0; i++, evn ^= true) {
				final int to = From + offset[i];
				if (OFF_BOARD(to))
					continue;
				final boolean val = evn? CAPTURE_MOVE(square[From], square[to]) : (square[to] == Piece.EMPTY);
				if (val)
					list[next++] = to;
			}
		} else {
			next = genAll_xPawn(list, offset, From, type);
		}
		list[next] = -1;
		return list;
	}

	int[] genCapture(final int From)
	{
		final int type = Math.abs(square[From]);
		final int[] offset = offsets[type];
		int next = 0;

		if (type == Piece.PAWN) {
			// captures
			for (int i = 0; offset[i] != 0; i += 2) {
				final int to = From + offset[i];
				if (OFF_BOARD(to))
					continue;
				else if (CAPTURE_MOVE(square[From], square[to]))
					list[next++] = to;
			}
		} else {
			next = genCapture_xPawn(list, offset, From, type);
		}
		list[next] = -1;
		return list;
	}

	int[] genMove(final int From)
	{
		final int type = Math.abs(square[From]);
		final int[] offset = offsets[type];
		int next = 0;

		if (type == Piece.PAWN) {
			// moves
			for (int i = 1; offset[i] != 0; i += 2) {
				final int to = From + offset[i];
				if (OFF_BOARD(to))
					continue;
				else if (square[to] == Piece.EMPTY)
					list[next++] = to;
			}
		} else {
			next = genMove_xPawn(list, offset, From, type);
		}
		list[next] = -1;
		return list;
	}

	boolean fromto(final int From, final int To)
	{
		if (OFF_BOARD(From | To))
			return false;

		final int type = Math.abs(square[From]);
		final int[] offset = offsets[type];

		if (type == Piece.PAWN) {
			final int diff = Math.abs(From - To);
			for (int i = 0; i < 4; i++) {
				if (diff == offset[i])
					return ((i%2 != 0)? (square[To] == Piece.EMPTY) : CAPTURE_MOVE(square[From], square[To]));
			}
		} else {
			return fromto_xPawn(From, To, type, offset);
		}
		return false;
	}

	@Override
	public boolean attackLine_Bishop(final DistDB db, final int From, final int To)
	{
		final int offset = db.step * ((To > From)? 1:-1);
		for (int to = From + offset, k = 1; ON_BOARD(to); to += offset, k++) {
			if (square[to] == Piece.EMPTY)
				continue;
			else if (OWN_PIECE(square[From], square[to]))
				return false;
			else if (k == 1 && (Math.abs(square[to]) == Piece.PAWN || Math.abs(square[to]) == Piece.KING))
				return true;
			else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN)
				return true;
			break;
		}
		return false;
	}

	boolean isAttacked(final int From)
	{
		// BISHOP
		final int[] offset = offsets[Piece.BISHOP];
		for (int i = 0; offset[i] != 0; i++) {
			for (int to = From + offset[i], k = 1; ON_BOARD(to); to += offset[i], k++) {
				if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(square[From], square[to]))
					break;
				else if (k == 1 && (Math.abs(square[to]) == Piece.PAWN || Math.abs(square[to]) == Piece.KING))
					return true;
				else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else
					break;
			}
		}
		return isAttacked_xBishop(From, square[From]);
	}
}
