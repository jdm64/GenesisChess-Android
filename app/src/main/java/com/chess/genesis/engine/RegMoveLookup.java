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

abstract class RegMoveLookup extends BaseBoard
{
	@Override
	protected int[] genAll_Pawn(int From, int[] list)
	{
		int next = 0;
		if (square[From] == Piece.WHITE_PAWN) { // WHITE
			if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 15]))
				list[next++] = From + 15;
			if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 17]))
				list[next++] = From + 17;
			if (square[From + 16] == 0) {
				list[next++] = From + 16;
				if (From <= Piece.H2 && square[From + 32] == 0)
					list[next++] = From + 32;
			}
		} else { // BLACK
			if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 17]))
				list[next++] = From - 17;
			if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 15]))
				list[next++] = From - 15;
			if (square[From - 16] == 0) {
				list[next++] = From - 16;
				if (From >= Piece.A7 && square[From - 32] == 0)
					list[next++] = From - 32;
			}
		}
		list[next] = -1;
		return list;
	}

	@Override
	protected int[] genCapture_Pawn(int From, int[] list)
	{
		int next = 0;
		if (square[From] == Piece.WHITE_PAWN) { // WHITE
			if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 15]))
				list[next++] = From + 15;
			if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 17]))
				list[next++] = From + 17;
		} else { // BLACK
			if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 17]))
				list[next++] = From - 17;
			if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 15]))
				list[next++] = From - 15;
		}
		list[next] = -1;
		return list;
	}

	@Override
	protected int[] genMove_Pawn(int From, int[] list)
	{
		int next = 0;
		if (square[From] == Piece.WHITE_PAWN) { // WHITE
			if (square[From + 16] == 0) {
				list[next++] = From + 16;
				if (From <= Piece.H2 && square[From + 32] == 0)
					list[next++] = From + 32;
			}
		} else { // BLACK
			if (square[From - 16] == 0) {
				list[next++] = From - 16;
				if (From >= Piece.A7 && square[From - 32] == 0)
					list[next++] = From - 32;
			}
		}
		list[next] = -1;
		return list;
	}

	@Override
	protected boolean fromTo_Pawn(int From, int To)
	{
		if (square[From] == Piece.WHITE_PAWN) { // WHITE
			if (From + 15 == To && COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 15]))
				return true;
			if (From + 17 == To && COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 17]))
				return true;
			if (square[From + 16] == 0) {
				if (From + 16 == To)
					return true;
				return From + 32 == To && From <= Piece.H2 && square[From + 32] == 0;
			}
		} else { // BLACK
			if (From - 17 == To && COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 17]))
				return true;
			if (From - 15 == To && COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 15]))
				return true;
			if (square[From - 16] == 0) {
				if (From - 16 == To)
					return true;
				else
					return From - 32 == To && From >= Piece.A7 && square[From - 32] == 0;
			}
		}
		return false;
	}

	@Override
	public boolean attackLine_Bishop(int From, int To, DistDB db)
	{
		final int offset = db.step * ((To > From)? 1:-1);
		for (int to = From + offset, k = 1; ON_BOARD(to); to += offset, k++) {
			if (square[to] == Piece.EMPTY) {
				continue;
			} else if (OWN_PIECE(square[From], square[to])) {
				return false;
			} else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN) {
				return true;
			} else if (k == 1) {
				if (Math.abs(square[to]) == Piece.PAWN && square[From] * (to - From) > 0)
					return true;
				else if (Math.abs(square[to]) == Piece.KING)
					return true;
			}
			break;
		}
		return false;
	}

	@Override
	protected boolean isAttacked_Bishop(int From, int Color)
	{
		final int[] offset = offsets[Piece.BISHOP];
		for (int i = 0; offset[i] != 0; i++) {
			for (int to = From + offset[i], k = 1; ON_BOARD(to); to += offset[i], k++) {
				if (square[to] == Piece.EMPTY) {
					continue;
				} else if (OWN_PIECE(Color, square[to])) {
					break;
				} else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN) {
					return true;
				} else if (k == 1) {
					if (Math.abs(square[to]) == Piece.PAWN && Color * (to - From) > 0)
						return true;
					else if (Math.abs(square[to]) == Piece.KING)
						return true;
				}
				break;
			}
		}
		return false;
	}
}
