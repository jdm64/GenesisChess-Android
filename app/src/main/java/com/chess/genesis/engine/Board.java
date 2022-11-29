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

import android.util.*;

public interface Board
{
	int[] InitPieceType = {
	    Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
	    Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
	    Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_BISHOP,
	    Piece.BLACK_ROOK,   Piece.BLACK_ROOK,   Piece.BLACK_QUEEN,  Piece.BLACK_KING,
	    Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
	    Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
	    Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_BISHOP,
	    Piece.WHITE_ROOK,   Piece.WHITE_ROOK,   Piece.WHITE_QUEEN,  Piece.WHITE_KING};

	int PLACEOFFSET = 1000;

	int VALID_MOVE = 0;
	int INVALID_FORMAT = 1;
	int NOPIECE_ERROR = 2;
	int DONT_OWN = 3;
	int KING_FIRST = 4;
	int NON_EMPTY_PLACE = 5;
	int CAPTURE_OWN = 6;
	int INVALID_MOVEMENT = 7;
	int IN_CHECK = 8;
	int IN_CHECK_PLACE = 9;
	int CANT_CASTLE = 10;

	int MOVE_ALL = 0;
	int MOVE_CAPTURE = 1;
	int MOVE_MOVE = 2;
	int MOVE_PLACE = 3;

	int NOT_MATE = 0;
	int CHECK_MATE = 1;
	int STALE_MATE = 2;

	static int COL(final int x)
	{
		return x & 7;
	}

	static boolean ON_BOARD(final int sq)
	{
		return (sq & 0x88) == 0;
	}

	static boolean OFF_BOARD(final int sq)
	{
		return (sq & 0x88) != 0;
	}

	static boolean OWN_PIECE(final int A, final int B)
	{
		return (A * B >  0);
	}

	static boolean CAPTURE_MOVE(final int A, final int B)
	{
		return (A * B <  0);
	}

	static boolean ANY_MOVE(final int A, final int B)
	{
		return (A * B <= 0);
	}

	static int EE64(final int x)
	{
		return ((x & 7) + x) >> 1;
	}

	static int EE64F(final int x)
	{
		return ((7 - (x >> 4)) << 3) + (x & 7);
	}

	static int SF88(final int x)
	{
		return (x & ~7) + x;
	}

	static int SFF88(final int x)
	{
		return ((7 - (x >> 3)) << 4) + (x & 7);
	}

	Board copy();

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
			move += Move.PIECE_SYM[Math.abs(from - Board.PLACEOFFSET) + 6];
		} else {
			move += Move.printSq(from);
		}
		move += Move.printSq(to);

		var res = parseMove(move);
		return res.second == Board.VALID_MOVE ? res.first : null;
	}

	int eval();
	MoveList getMoveList(int stm, int type);
}
