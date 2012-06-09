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
	static final int ZBOX_SIZE = 838;
	static final int WTM_HASH = 837;
	static final int ENPASSANT_HASH = 834;
	static final int CASTLE_HASH = 834;
	static final int HOLD_START = 768;

	void reset();

	int Piece(int index);
	int PieceType(int index);

	int getStm();
	MoveFlags getMoveFlags();

	int[] getBoardArray();
	int[] getPieceCounts(final int Loc);

	void setStartHash(final long StartHash);
	long[] getHashBox();

	int kingIndex(final int color);
	boolean incheck(final int color);
	int isMate();

	String printZfen();

	void make(final Move move);
	void unmake(final Move move);
	void unmake(final Move move, final MoveFlags undoFlags);

	int validMove(final Move move);
	boolean validMove(final Move moveIn, final Move move);
}
