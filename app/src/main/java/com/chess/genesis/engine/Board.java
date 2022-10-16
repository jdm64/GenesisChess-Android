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

import java.util.function.*;
import android.util.*;

public interface Board
{
	Board copy();

	Supplier<Move> moveGenerator();
	Move newMove();
	void reset();

	int pieceLoc(int index);
	int pieceType(int index);

	long hash();
	int getPly();
	int getStm();
	void getMoveFlags(final MoveFlags Flags);

	int[] getBoardArray();
	int[] getPieceCounts(final int Loc);

	MoveListPool getMoveListPool();
	void setStartHash(final long StartHash);
	long[] getHashBox();

	int kingIndex(final int color);
	boolean inCheck(final int color);
	int isMate();

	String printZFen();
	boolean parseZFen(String zFen);

	void make(final Move move);
	void unmake(final Move move);
	void unmake(final Move move, final MoveFlags UndoFlags);

	Pair<Move,Integer> parseMove(String moveStr);
	boolean validMove(final Move moveIn, final Move move);

	default Move parseMove(int from, int to)
	{
		var move = "";
		if (from > 0x88) {
			move += Move.pieceSymbol[Math.abs(from - Move.PLACEOFFSET)];
		} else {
			move += Move.printSq(from);
		}
		move += Move.printSq(to);

		var res = parseMove(move);
		return res.second == Move.VALID_MOVE ? res.first : null;
	}

	int eval();
	MoveList getMoveList(int stm, int type);
}
