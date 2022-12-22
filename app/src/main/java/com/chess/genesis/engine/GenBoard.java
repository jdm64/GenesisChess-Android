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

import static com.chess.genesis.engine.Board.*;
import androidx.core.util.*;

public class GenBoard extends BaseBoard
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

	private static final long[] hashBox = new long[ZBOX_SIZE];
	private static long startHash;

	private final MoveNode item = new MoveNode();

	public GenBoard()
	{
		reset();
	}

	private GenBoard(final GenBoard board)
	{
		square = IntArray.clone(board.square);
		piece = IntArray.clone(board.piece);
		pieceType = IntArray.clone(board.pieceType);

		stm = board.stm;
		ply = board.ply;
		key = board.key;
		mScore = board.mScore;
	}

	@Override
	public GenBoard copy()
	{
		return new GenBoard(this);
	}

	private int pieceIndex(final int loc, final int type)
	{
		var idx = (type < 0)? 0 : 16;
		var piece_type = Math.abs(type);
		var start = idx + idxOffset[piece_type];
		var end = idx + idxOffset[piece_type + 1];

		for (var i = start; i < end; i++) {
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
		pieceType = IntArray.clone(InitPieceType);
		for (int i = 0; i < 32; i++)
			piece[i] = Piece.PLACEABLE;

		mScore = 0;
		ply = 0;
		stm = Piece.WHITE;
		key = startHash;
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
	public void make(final Move move)
	{
		// update board information
		square[move.to] = pieceType[move.index];
		mScore += stm * locValue[Math.abs(square[move.to])][EE64(move.to)];
		if (move.from != Piece.PLACEABLE) {
			mScore -= stm * locValue[Math.abs(square[move.from])][EE64(move.from)];
			square[move.from] = Piece.EMPTY;
		}
		// update piece information
		piece[move.index] = move.to;
		if (move.xindex != Piece.NONE) {
			mScore += stm * locValue[Math.abs(pieceType[move.xindex])][EE64(move.to)];
			mScore += stm * pieceValue[Math.abs(pieceType[move.xindex])];
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
	public void unmake(final Move move, MoveFlags undoFlags)
	{
		piece[move.index] = move.from;
		mScore += stm * locValue[Math.abs(square[move.to])][EE64(move.to)];
		if (move.xindex == Piece.NONE) {
			square[move.to] = Piece.EMPTY;
		} else {
			square[move.to] = pieceType[move.xindex];
			piece[move.xindex] = move.to;
			mScore += stm * locValue[Math.abs(pieceType[move.xindex])][EE64(move.to)];
			mScore += stm * pieceValue[Math.abs(pieceType[move.xindex])];
		}
		if (move.from != Piece.PLACEABLE) {
			square[move.from] = pieceType[move.index];
			mScore -= stm * locValue[Math.abs(square[move.from])][EE64(move.from)];
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

	@Override
	public boolean validMove(final Move moveIn, final Move move)
	{
		if (moveIn.isNull())
			return false;
		move.set(moveIn);

		if ((move.index = pieceIndex(move.from, pieceType[move.index])) == Piece.NONE)
			return false;
		if (pieceType[move.index] * stm <= 0)
			return false;
		if (move.xindex != Piece.NONE) {
			if ((move.xindex = pieceIndex(move.to, pieceType[move.xindex])) == Piece.NONE)
				return false;
		} else if (square[move.to] != Piece.EMPTY) {
			return false;
		}

		if (move.from != Piece.PLACEABLE && !fromTo(move.from, move.to))
				return false;
		if (ply < 2 && Math.abs(pieceType[move.index]) != Piece.KING)
			return false;

		boolean ret = true;

		make(move);
		if (inCheck(stm ^ -2))
			ret = false;
		if (move.from == Piece.PLACEABLE && inCheck(stm))
			ret = false;
		unmake(move, undoFlags);

		return ret;
	}

	@Override
	public Pair<Move,Integer> parseMove(String moveStr)
	{
		var move = new Move();
		if (!move.parse(moveStr)) {
			return new Pair<>(move, INVALID_FORMAT);
		}

		// setup move.(x)index
		if (move.from == Piece.PLACEABLE) {
			move.index = pieceIndex(Piece.PLACEABLE, move.getPlace() * stm);
			if (move.index == Piece.NONE)
				return new Pair<>(move, NOPIECE_ERROR);
			move.xindex = pieceIndex(move.to);
			if (move.xindex != Piece.NONE)
				return new Pair<>(move, NON_EMPTY_PLACE);
		} else {
			move.index = pieceIndex(move.from);
			if (move.index == Piece.NONE)
				return new Pair<>(move, NOPIECE_ERROR);
			else if (square[move.from] * stm < 0)
				return new Pair<>(move, DONT_OWN);
			move.xindex = pieceIndex(move.to);
			if (move.xindex != Piece.NONE && square[move.to] * stm > 0)
				return new Pair<>(move, CAPTURE_OWN);
		}
		// must place king first
		if (ply < 2 && Math.abs(pieceType[move.index]) != Piece.KING)
			return new Pair<>(move, KING_FIRST);

		if (move.from != Piece.PLACEABLE && !fromTo(move.from, move.to))
			return new Pair<>(move, INVALID_MOVEMENT);
		int ret = VALID_MOVE;

		make(move);
		// curr is opponent after make
		if (inCheck(stm ^ -2))
			ret = IN_CHECK;
		else if (move.from == Piece.PLACEABLE && inCheck(stm))
			ret = IN_CHECK_PLACE;
		unmake(move, undoFlags);

		return new Pair<>(move, ret);
	}

	@Override
	protected void parseReset()
	{
		for (var i = 0; i < 128; i++)
			square[i] = Piece.EMPTY;
		for (var i = 0; i < 32; i++) {
			piece[i] = Piece.DEAD;
			pieceType[i] = InitPieceType[i];
		}
	}

	@Override
	protected void setMaxPly()
	{
		var tPly = 0;
		for (var i = 0; i < 32; i++) {
			if (piece[i] == Piece.DEAD)
				tPly += 2;
			else if (piece[i] != Piece.PLACEABLE)
				tPly++;
		}
		ply = Math.max(ply, tPly);

		if (stm == Piece.WHITE) {
			if (ply % 2 != 0)
				ply++;
		} else if (ply % 2 == 0) {
			ply++;
		}
	}

	@Override
	protected boolean setPiece(int loc, int type)
	{
		var idx = type < 0 ? 0 : 16;
		var abs_type = Math.abs(type);
		var start = idx + idxOffset[abs_type];
		var end = idx + idxOffset[abs_type + 1];

		for (var i = start; i < end; i++) {
			if (piece[i] == Piece.DEAD) {
				piece[i] = loc;
				if (loc != Piece.PLACEABLE)
					square[loc] = type;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean inCheck(int color)
	{
		var king = (color == Piece.WHITE)? 31:15;
		return (piece[king] != Piece.PLACEABLE) && isAttacked(piece[king], color);
	}

	@Override
	protected int parseFen_Specific(int n, String pos, boolean isZFen)
	{
		var div = isZFen ? ':' : ' ';
		// parse placeable pieces
		var st = pos.toCharArray();
		for (;; n++) {
			if (st[n] == div || st[n] == '-') {
				n++;
				break;
			} else if (!Character.isLetter(st[n]) || !setPiece(Piece.PLACEABLE, stype[st[n] % 21])) {
				return -1;
			}
		}
		return n;
	}

	@Override
	protected void printFen_Specific(StringBuilder fen, boolean isZFen)
	{
		var hasPlace = false;
		for (var i = 0; i < 32; i++) {
			if (piece[i] == Piece.PLACEABLE) {
				fen.append(Move.PIECE_SYM[InitPieceType[i] + 6]);
				hasPlace = true;
			}
		}
		if (!hasPlace && !isZFen) {
			fen.append('-');
		}
	}

	@Override
	protected int[] genAll_Pawn(int From, int[] list)
	{
		var next = 0;
		var evn = true;
		var from_piece = square[From];
		for (var diff : QUEEN_OFFSETS) {
			var to = From + diff;
			if (ON_BOARD(to) && (evn ? CAPTURE_MOVE(from_piece, square[to]) : (square[to] == Piece.EMPTY))) {
				list[next++] = to;
			}
			evn ^= true;
		}
		list[next] = -1;
		return list;
	}

	@Override
	protected int[] genCapture_Pawn(int From, int[] list)
	{
		var next = 0;
		var evn = true;
		for (var diff : QUEEN_OFFSETS) {
			if (evn) {
				var to = From + diff;
				if (ON_BOARD(to) && CAPTURE_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			evn ^= true;
		}
		list[next] = -1;
		return list;
	}

	@Override
	protected int[] genMove_Pawn(int From, int[] list)
	{
		var next = 0;
		var evn = true;
		for (var diff : QUEEN_OFFSETS) {
			if (!evn) {
				var to = From + diff;
				if (ON_BOARD(to) && square[to] == Piece.EMPTY)
					list[next++] = to;
			}
			evn ^= true;
		}
		list[next] = -1;
		return list;
	}

	@Override
	protected boolean fromTo_Pawn(int From, int To)
	{
		var diff = From - To;
		var idx = IntArray.indexOf(QUEEN_OFFSETS, diff);
		if (idx >= 0) {
			return idx % 2 == 0 ? CAPTURE_MOVE(square[From], square[To]) : (square[To] == Piece.EMPTY);
		}
		return false;
	}

	@Override
	public boolean attackLine_Bishop(int From, int offset)
	{
		for (int to = From + offset, k = 1; ON_BOARD(to); to += offset, k++) {
			if (CAPTURE_MOVE(square[From], square[to])) {
				var to_piece = Math.abs(square[to]);
				if (to_piece == Piece.BISHOP || to_piece == Piece.QUEEN) {
					return true;
				}
				return k == 1 && (to_piece == Piece.PAWN || to_piece == Piece.KING);
			} else if (OWN_PIECE(square[From], square[to])) {
				return false;
			}
		}
		return false;
	}

	@Override
	protected boolean isAttacked_Bishop(int From, int Color)
	{
		for (var diff : BISHOP_OFFSETS) {
			for (int to = From + diff, k = 1; ON_BOARD(to); to += diff, k++) {
				var to_piece = square[to];
				var to_type = Math.abs(to_piece);
				if (CAPTURE_MOVE(Color, to_piece)) {
					if (to_type == Piece.BISHOP || to_type == Piece.QUEEN || (k == 1 && (to_type == Piece.PAWN || to_type == Piece.KING))) {
						return true;
					}
					break;
				} else if (OWN_PIECE(Color, to_piece)) {
					break;
				}
			}
		}
		return false;
	}

	private void getMoveList(final MoveList data, final int color, final int moveType)
	{
		final boolean stmCk = inCheck(color);
		final int start = (color == Piece.WHITE)? 31:15, end = (color == Piece.WHITE)? 16:0;

		for (int idx = start; idx >= end; idx--) {
			if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
				continue;

			final int[] loc;
			switch (moveType) {
			case MOVE_ALL:
			default:
				loc = genAll(piece[idx]);
				break;
			case MOVE_CAPTURE:
				loc = genCapture(piece[idx]);
				break;
			case MOVE_MOVE:
				loc = genMove(piece[idx]);
				break;
			}

			for (int n = 0; loc[n] != -1; n++) {
				item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
				item.move.to = loc[n];
				item.move.from = piece[idx];
				item.move.index = idx;

				make(item.move);
				if (!inCheckMove(item.move, color, stmCk)) {
					item.check = inCheckMove(item.move, color ^ -2, false);
					item.score = eval();
					data.add(item);
				}
				unmake(item.move, undoFlags);
			}
		}
	}

	private void getPlaceMoveList(final MoveList data, final int pieceType)
	{
		final int idx = pieceIndex(Piece.PLACEABLE, pieceType);

		if (idx == Piece.NONE)
			return;

		final int color = pieceType / Math.abs(pieceType);
		final boolean stmCk = inCheck(color);
		for (int loc = 0x77; loc >= 0; loc--) {
			if (OFF_BOARD(loc)) {
				loc -= 7;
				continue;
			} else if (square[loc] != Piece.EMPTY) {
				continue;
			}
			item.move.index = idx;
			item.move.to = loc;
			item.move.xindex = Piece.NONE;
			item.move.from = Piece.PLACEABLE;
			item.move.setPlace(Math.abs(pieceType));

			make(item.move);
			// place moves are only valid if neither side is inCheck
			if (!inCheckMove(item.move, color, stmCk) && !inCheckMove(item.move, color ^ -2, false)) {
				// item.check initialized to false
				item.score = eval();
				data.add(item);
			}
			unmake(item.move, undoFlags);
		}
	}

	@Override
	public MoveList getMoveList(final int color, final int moveType)
	{
		final MoveList data = pool.get();
		data.size = 0;

		switch (moveType) {
		case MOVE_ALL:
			if (ply < 2) {
				getPlaceMoveList(data, Piece.KING * color);
				break;
			}
			getMoveList(data, color, MOVE_ALL);
			for (int type = Piece.QUEEN; type >= Piece.PAWN; type--)
				getPlaceMoveList(data, type * color);
			break;
		case MOVE_CAPTURE:
		case MOVE_MOVE:
			getMoveList(data, color, moveType);
			break;
		case MOVE_PLACE:
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
