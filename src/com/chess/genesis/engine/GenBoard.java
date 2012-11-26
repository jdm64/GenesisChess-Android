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

import com.chess.genesis.util.*;

public class GenBoard extends GenPosition implements Board
{
	private static final int[][] locValue = {
	{	0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0},
	{	-5, 0, 0, 0, 0, 0, 0, -5,
/* Pawn */	 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		-5, 0, 0, 0, 0, 0, 0, -5},
	{	-10, -5,  0,  0,  0,  0, -5, -10,
/* Knight */	 -5,  0, 10, 10, 10, 10,  0,  -5,
		  0, 10, 20, 20, 20, 20, 10,   0,
		  0, 10, 20, 20, 20, 20, 10,   0,
		  0, 10, 20, 20, 20, 20, 10,   0,
		  0, 10, 20, 20, 20, 20, 10,   0,
		 -5,  0, 10, 10, 10, 10,  0,  -5,
		-10, -5,  0,  0,  0,  0, -5, -10},
	{	-10, -10, -10, -10, -10, -10, -10, -10,
/* Bishop */	-10,   0,   0,   0,   0,   0,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,   0,   0,   0,   0,   0, -10,
		-10, -10, -10, -10, -10, -10, -10, -10},
	{	-10, -10, -10, -10, -10, -10, -10, -10,
/* Rook */	-10,   0,   0,   0,   0,   0,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,   0,   0,   0,   0,   0, -10,
		-10, -10, -10, -10, -10, -10, -10, -10},
	{	-10, -10, -10, -10, -10, -10, -10, -10,
/* Queen */	-10,   0,   0,   0,   0,   0,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,   0,   0,   0,   0,   0, -10,
		-10, -10, -10, -10, -10, -10, -10, -10},
	{	-10,  0,  0,  0,  0,  0,  0, -10,
/* King */	  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		-10,  0,  0,  0,  0,  0,  0, -10}
	};

	private static final int[] typeLookup = {
		0, 0, 0, 0, 0, 0,  0,  0,
		1, 1, 2, 2, 3, 3,  4,  5,
		6, 6, 6, 6, 6, 6,  6,  6,
		7, 7, 8, 8, 9, 9, 10, 11};

	private static final int[] pieceValue =
		{0, 224, 336, 560, 896, 1456, 0};

	// for pieceIndex()
	private final static int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};

	public static final long[] hashBox = new long[ZBOX_SIZE];
	public static long startHash;
	private static final Move moveType = new GenMove();

	private final MoveNode item = new MoveNode(new GenMove());
	private final MoveListPool pool;
	private long key;
	private int mscore;

	public GenBoard()
	{
		reset();
		pool = new MoveListPool(new GenMove());
	}

	public GenBoard(final GenBoard board)
	{
		square = IntArray.clone(board.square);
		piece = IntArray.clone(board.piece);
		piecetype = IntArray.clone(board.piecetype);
		pool = board.getMoveListPool();

		stm = board.stm;
		ply = board.ply;
		key = board.key;
		mscore = board.mscore;
	}

	@Override
	public GenBoard clone()
	{
		return new GenBoard(this);
	}

	@Override
	public Move newMove()
	{
		return moveType.newInstance();
	}

	private int pieceIndex(final int loc, final int type)
	{
		final int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];

		for (int i = start; i < end; i++) {
			if (piece[i] == loc)
				return i;
		}
		return Piece.NONE;
	}

	private int pieceIndex(final int loc)
	{
		for (int i = 0; i < 32; i++)
			if (piece[i] == loc)
				return i;
		return Piece.NONE;
	}

	@Override
	public final void reset()
	{
		square = new int[128];
		piece = new int[32];
		piecetype = IntArray.clone(Move.InitPieceType);
		for (int i = 0; i < 32; i++)
			piece[i] = Piece.PLACEABLE;

		mscore = 0;
		ply = 0;
		stm = Piece.WHITE;
		key = startHash;
	}

	@Override
	public int Piece(final int index)
	{
		return piece[index];
	}

	@Override
	public int PieceType(final int index)
	{
		return piecetype[index];
	}

	// Do Not call the following functions!
	@Override
	public void getMoveFlags(final MoveFlags Flags)
	{
		throw new RuntimeException("RegBoard function called from GenBoard class");
	}
	@Override
	public void unmake(final Move move, final MoveFlags undoFlags)
	{
		throw new RuntimeException("RegBoard function called from GenBoard class");
	}
	// ------

	@Override
	public int getStm()
	{
		return stm;
	}

	@Override
	public int getPly()
	{
		return ply;
	}

	@Override
	public long hash()
	{
		return key;
	}

	@Override
	public long[] getHashBox()
	{
		return hashBox;
	}

	@Override
	public void setStartHash(final long StartHash)
	{
		startHash = StartHash;
	}

	@Override
	public MoveListPool getMoveListPool()
	{
		return pool;
	}

	@Override
	public int kingIndex(final int color)
	{
		return (Piece.WHITE == color)? piece[31] : piece[15];
	}

	@Override
	public int[] getPieceCounts(final int Loc)
	{
		final int[] counts = new int[13];

		for (int i = 0; i < 32; i++) {
			if (piece[i] == Loc)
				counts[piecetype[i] + 6]++;
		}
		return counts;
	}

	@Override
	public int[] getBoardArray()
	{
		return square;
	}

	@Override
	public void make(final Move move)
	{
		// update board information
		square[move.to] = piecetype[move.index];
		mscore += stm * locValue[Math.abs(square[move.to])][EE64F(move.to)];
		if (move.from != Piece.PLACEABLE) {
			mscore -= stm * locValue[Math.abs(square[move.from])][EE64F(move.from)];
			square[move.from] = Piece.EMPTY;
		}
		// update piece information
		piece[move.index] = move.to;
		if (move.xindex != Piece.NONE) {
			mscore += stm * locValue[Math.abs(piecetype[move.xindex])][EE64F(move.to)];
			mscore += stm * pieceValue[Math.abs(piecetype[move.xindex])];
			piece[move.xindex] = Piece.DEAD;
		}

		final int to = EE64(move.to);
		final int from = EE64(move.from);

		key += (stm == Piece.WHITE)? -hashBox[WTM_HASH] : hashBox[WTM_HASH];
		key += hashBox[12 * to + typeLookup[move.index]];
		if (move.from != Piece.PLACEABLE)
			key -= hashBox[12 * from + typeLookup[move.index]];
		else
			key -= hashBox[HOLD_START + typeLookup[move.index]];
		if (move.xindex != Piece.NONE)
			key -= hashBox[12 * to + typeLookup[move.xindex]];

		stm ^= -2;
		ply++;
	}

	@Override
	public void unmake(final Move move)
	{
		piece[move.index] = move.from;
		mscore += stm * locValue[Math.abs(square[move.to])][EE64F(move.to)];
		if (move.xindex == Piece.NONE) {
			square[move.to] = Piece.EMPTY;
		} else {
			square[move.to] = piecetype[move.xindex];
			piece[move.xindex] = move.to;
			mscore += stm * locValue[Math.abs(piecetype[move.xindex])][EE64F(move.to)];
			mscore += stm * pieceValue[Math.abs(piecetype[move.xindex])];
		}
		if (move.from != Piece.PLACEABLE) {
			square[move.from] = piecetype[move.index];
			mscore -= stm * locValue[Math.abs(square[move.from])][EE64F(move.from)];
		}

		final int to = EE64(move.to);
		final int from = EE64(move.from);

		key += (stm == Piece.WHITE)? -hashBox[WTM_HASH] : hashBox[WTM_HASH];
		key -= hashBox[12 * to + typeLookup[move.index]];
		if (move.from != Piece.PLACEABLE)
			key += hashBox[12 * from + typeLookup[move.index]];
		else
			key += hashBox[HOLD_START + typeLookup[move.index]];
		if (move.xindex != Piece.NONE)
			key += hashBox[12 * to + typeLookup[move.xindex]];

		stm ^= -2;
		ply--;
	}

	private boolean incheckMove(final Move move, final int color, final boolean stmCk)
	{
		final int king = (color == Piece.WHITE)? 31:15;
		if (stmCk || move.index == king)
			return incheck(color);
		return (attackLine(piece[king], move.from) || attackLine(piece[king], move.to));
	}

	@Override
	public int isMate()
	{
		if (getMoveList(stm, Move.MOVE_ALL).size != 0)
			return Move.NOT_MATE;
		else if (incheck(stm))
			return Move.CHECK_MATE;
		return Move.STALE_MATE;
	}

	@Override
	public boolean validMove(final Move moveIn, final Move move)
	{
		move.set(moveIn);

		if ((move.index = pieceIndex(move.from, piecetype[move.index])) == Piece.NONE)
			return false;
		if (piecetype[move.index] * stm <= 0)
			return false;
		if (move.xindex != Piece.NONE) {
			if ((move.xindex = pieceIndex(move.to, piecetype[move.xindex])) == Piece.NONE)
				return false;
		} else if (square[move.to] != Piece.EMPTY) {
			return false;
		}

		if (move.from != Piece.PLACEABLE && !fromto(move.from, move.to))
				return false;
		if (ply < 2 && Math.abs(piecetype[move.index]) != Piece.KING)
			return false;

		boolean ret = true;

		make(move);
		if (incheck(stm ^ -2))
			ret = false;
		if (move.from == Piece.PLACEABLE && incheck(stm))
			ret = false;
		unmake(move);

		return ret;
	}

	@Override
	public int validMove(final Move move)
	{
		// setup move.(x)index
		if (move.from == Piece.PLACEABLE) {
			move.index = pieceIndex(Piece.PLACEABLE, move.index * stm);
			if (move.index == Piece.NONE)
				return Move.NOPIECE_ERROR;
			move.xindex = pieceIndex(move.to);
			if (move.xindex != Piece.NONE)
				return Move.NON_EMPTY_PLACE;
		} else {
			move.index = pieceIndex(move.from);
			if (move.index == Piece.NONE)
				return Move.NOPIECE_ERROR;
			else if (square[move.from] * stm < 0)
				return Move.DONT_OWN;
			move.xindex = pieceIndex(move.to);
			if (move.xindex != Piece.NONE && square[move.to] * stm > 0)
				return Move.CAPTURE_OWN;
		}
		// must place king first
		if (ply < 2 && Math.abs(piecetype[move.index]) != Piece.KING)
			return Move.KING_FIRST;

		if (move.from != Piece.PLACEABLE && !fromto(move.from, move.to))
				return Move.INVALID_MOVEMENT;
		int ret = Move.VALID_MOVE;

		make(move);
		// curr is opponent after make
		if (incheck(stm ^ -2))
			ret = Move.IN_CHECK;
		else if (move.from == Piece.PLACEABLE && incheck(stm))
			ret = Move.IN_CHECK_PLACE;
		unmake(move);

		return ret;
	}

	@Override
	public int eval()
	{
		return (stm == Piece.WHITE)? -mscore : mscore;
	}

	private void getMoveList(final MoveList data, final int color, final int movetype)
	{
		final boolean stmCk = incheck(color);
		final int start = (color == Piece.WHITE)? 31:15, end = (color == Piece.WHITE)? 16:0;

		for (int idx = start; idx >= end; idx--) {
			if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
				continue;

			int[] loc;
			switch (movetype) {
			case Move.MOVE_ALL:
			default:
				loc = genAll(piece[idx]);
				break;
			case Move.MOVE_CAPTURE:
				loc = genCapture(piece[idx]);
				break;
			case Move.MOVE_MOVE:
				loc = genMove(piece[idx]);
				break;
			}

			for (int n = 0; loc[n] != -1; n++) {
				item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
				item.move.to = loc[n];
				item.move.from = piece[idx];
				item.move.index = idx;

				make(item.move);
				if (!incheckMove(item.move, color, stmCk)) {
					item.check = incheckMove(item.move, color ^ -2, false);
					item.score = eval();
					data.add(item);
				}
				unmake(item.move);
			}
		}
	}

	private void getPlaceMoveList(final MoveList data, final int pieceType)
	{
		final int idx = pieceIndex(Piece.PLACEABLE, pieceType);

		if (idx == Piece.NONE)
			return;

		final int color = pieceType / Math.abs(pieceType);
		final boolean stmCk = incheck(color);
		for (int loc = 0x77; loc >= 0; loc--) {
			if ((loc & 0x88) != 0) {
				loc -= 7;
				continue;
			} else if (square[loc] != Piece.EMPTY) {
				continue;
			}
			item.move.index = idx;
			item.move.to = loc;
			item.move.xindex = Piece.NONE;
			item.move.from = Piece.PLACEABLE;

			make(item.move);
			// place moves are only valid if neither side is inCheck
			if (!incheckMove(item.move, color, stmCk) && !incheckMove(item.move, color ^ -2, false)) {
				// item.check initialized to false
				item.score = eval();
				data.add(item);
			}
			unmake(item.move);
		}
	}

	@Override
	public MoveList getMoveList(final int color, final int movetype)
	{
		final MoveList data = pool.get();
		data.size = 0;

		switch (movetype) {
		case Move.MOVE_ALL:
			if (ply < 2) {
				getPlaceMoveList(data, Piece.KING * color);
				break;
			}
			getMoveList(data, color, Move.MOVE_ALL);
			for (int type = Piece.QUEEN; type >= Piece.PAWN; type--)
				getPlaceMoveList(data, type * color);
			break;
		case Move.MOVE_CAPTURE:
		case Move.MOVE_MOVE:
			getMoveList(data, color, movetype);
			break;
		case Move.MOVE_PLACE:
			if (ply < 2) {
				getPlaceMoveList(data, Piece.KING * color);
				break;
			}
			for (int type = Piece.QUEEN; type >= Piece.PAWN; type--)
				getPlaceMoveList(data, type * color);
			break;
		}
		return data;
	}
}
