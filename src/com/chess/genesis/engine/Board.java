/*	GenChess, a genesis chess engine
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

public interface Board
{
	void reset();

	int Piece(int index);
	int PieceType(int index);

	int getStm();
	MoveFlags getMoveFlags();

	int[] getBoardArray();
	int[] getPieceCounts(final int Loc);

	int kingIndex(final int color);
	boolean incheck(final int color);
	int isMate();

	String printZfen();

	void make(final GenMove move);
	void make(final RegMove move);

	void unmake(final GenMove move);
	void unmake(final RegMove move, final MoveFlags undoFlags);

	int validMove(final GenMove move);
	int validMove(final RegMove move);

	boolean validMove(final GenMove moveIn, final GenMove move);
	boolean validMove(final RegMove moveIn, final RegMove move);
}
