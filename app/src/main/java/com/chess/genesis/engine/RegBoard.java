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

public class RegBoard extends BaseBoard
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
	{	 0,   0,   0,   0,   0,   0,   0,   0,
/* Pawn */	 5,   5,   5,   5,   5,   5,   5,   5,
		-5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,
		 5,   5,  10,  10,  10,  10,   5,   5,
		 5,   5,  10,  10,  10,  10,   5,   5,
		-5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,
		 5,   5,   5,   5,   5,   5,   5,   5,
		 0,   0,   0,   0,   0,   0,   0,   0},
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
	{	-20, -10,  20,  10,  20,  10,  20, -20,
/* King */	-10, -10,  -5,  -5,  -5,  -5, -10, -10,
		-10,  -5,  10,  10,  10,  10,  -5, -10,
		-10,  -5,  10,  20,  20,  10,  -5, -10,
		-10,  -5,  10,  20,  20,  10,  -5, -10,
		-10,  -5,  10,  10,  10,  10,  -5, -10,
		-10, -10,  -5,  -5,  -5,  -5, -10, -10,
		-20, -10,  20,  10,  20,  10,  20, -20}
	};

	public static final int[] InitRegPiece = {
		Piece.A7, Piece.B7, Piece.C7, Piece.D7, Piece.E7, Piece.F7, Piece.G7, Piece.H7,
		Piece.B8, Piece.G8, Piece.C8, Piece.F8, Piece.A8, Piece.H8, Piece.D8, Piece.E8,
		Piece.A2, Piece.B2, Piece.C2, Piece.D2, Piece.E2, Piece.F2, Piece.G2, Piece.H2,
		Piece.B1, Piece.G1, Piece.C1, Piece.F1, Piece.A1, Piece.H1, Piece.D1, Piece.E1};

	private static final long[] hashBox = new long[ZBOX_SIZE];
	private static long startHash;
	private static final Move moveType = new RegMove();
	private static final MoveListPool pool = new MoveListPool(moveType);

	private final MoveNode item = new MoveNode(moveType);
	private final MoveFlags undoFlags = new MoveFlags();
	private final MoveFlags flags = new MoveFlags();

	public RegBoard()
	{
		reset();
	}

	private RegBoard(final RegBoard board)
	{
		square = IntArray.clone(board.square);
		piece = IntArray.clone(board.piece);
		pieceType = IntArray.clone(board.pieceType);

		stm = board.stm;
		ply = board.ply;
		key = board.key;
		mScore = board.mScore;
		flags.set(board.flags);
	}

	@Override
	public RegBoard copy()
	{
		return new RegBoard(this);
	}

	@Override
	public Supplier<Move> moveGenerator()
	{
		return moveType;
	}

	@Override
	public Move newMove()
	{
		return moveType.get();
	}

	private int pieceIndex(final int loc, final int type)
	{
		final int start = (type > 0)? 16:0, end = (type > 0)? 32:16;

		for (int i = start; i < end; i++) {
			if (piece[i] == loc && pieceType[i] == type)
				return i;
		}
		return Piece.NONE;
	}

	@Override
	public final void reset()
	{
		square = new int[128];
		piece = IntArray.clone(InitRegPiece);
		pieceType = IntArray.clone(Move.InitPieceType);
		for (int i = 0; i < 32; i++)
			square[piece[i]] = pieceType[i];

		mScore = 0;
		ply = 0;
		stm = Piece.WHITE;
		key = startHash;
		flags.reset();
	}

	// Do Not call the following functions!
	@Override
	public void unmake(final Move move)
	{
		throw new RuntimeException("GenBoard function called from RegBoard");
	}
	// ------

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
	public void getMoveFlags(final MoveFlags Flags)
	{
		Flags.set(flags);
	}

	private static boolean isPromote(final Move move, final int color)
	{
		return (color == Piece.WHITE)? (move.to >= Piece.A8) : (move.to <= Piece.H1);
	}

	@Override
	public void make(final Move move)
	{
		final boolean isWhite = (move.index > 15);
		final int color = isWhite? Piece.WHITE : Piece.BLACK;

		key ^= hashBox[13 * EE64(move.from) + pieceType[move.index] + 6];

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == Move.CASTLE_QS);
			final int castleTo = move.to + (left? 1 : -1);
			final int castleI = pieceIndex(move.to - (move.to & Move.EP_FILE) + (left? 0 : 7), color * Piece.ROOK);

			key ^= hashBox[13 * EE64(piece[castleI]) + pieceType[castleI] + 6];
			key ^= hashBox[13 * EE64(castleTo) + pieceType[castleI] + 6];
			if (flags.canKingCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color];
			if (flags.canQueenCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color * 2];

			square[castleTo] = pieceType[castleI];
			square[piece[castleI]] = Piece.EMPTY;
			piece[castleI] = castleTo;
			mScore += stm * locValue[Piece.ROOK][EE64(castleTo)];
			mScore -= stm * locValue[Piece.ROOK][EE64(castleI)];
			flags.clearCastle(color);
		} else if (Math.abs(pieceType[move.index]) == Piece.ROOK) {
			if (move.from == (isWhite? Piece.H1:Piece.H8) && flags.canKingCastle(color) != 0) {
				flags.clearKingCastle(color);
				key ^= hashBox[CASTLE_HASH + color];
			} else if (move.from == (isWhite? Piece.A1:Piece.A8) && flags.canQueenCastle(color) != 0) {
				flags.clearQueenCastle(color);
				key ^= hashBox[CASTLE_HASH + color * 2];
			}
		} else if (Math.abs(pieceType[move.index]) == Piece.KING && flags.canCastle(color) != 0) {
			if (flags.canKingCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color];
			if (flags.canQueenCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color * 2];

			flags.clearCastle(color);
		} else if (move.getPromote() != 0) {
			pieceType[move.index] = move.getPromote() * color;
		}
		key ^= hashBox[13 * EE64(move.to) + pieceType[move.index] + 6];

		if (flags.canEnPassant() != 0) {
			flags.clearEnPassant();
			key ^= hashBox[ENPASSANT_HASH];
		}

		// update board information
		square[move.to] = pieceType[move.index];
		mScore += stm * locValue[Math.abs(square[move.to])][EE64(move.to)];
		mScore -= stm * locValue[Math.abs(square[move.from])][EE64(move.from)];
		square[move.from] = Piece.EMPTY;
		// update piece information
		piece[move.index] = move.to;
		if (move.xindex != Piece.NONE) {
			key ^= hashBox[13 * EE64(piece[move.xindex]) + pieceType[move.xindex] + 6];
			mScore += stm * locValue[Math.abs(pieceType[move.xindex])][EE64(piece[move.xindex])];
			mScore += stm * pieceValue[Math.abs(pieceType[move.xindex])];

			if (move.getEnPassant())
				square[piece[move.xindex]] = Piece.EMPTY;
			piece[move.xindex] = Piece.DEAD;
		} else if (Math.abs(pieceType[move.index]) == Piece.PAWN && Math.abs(move.to - move.from) == 32) {
			flags.setEnPassant(move.to & Move.EP_FILE);
			key ^= hashBox[ENPASSANT_HASH];
		}

		key ^= hashBox[WTM_HASH];
		stm ^= -2;
		ply++;
	}

	@Override
	public void unmake(final Move move, final MoveFlags UndoFlags)
	{
		final boolean isWhite = (move.index > 15);
		final int color = isWhite? Piece.WHITE : Piece.BLACK,
			bits = flags.bits ^ UndoFlags.bits;

		key ^= ((bits & (isWhite? Move.WK_CASTLE : Move.BK_CASTLE)) != 0)? hashBox[CASTLE_HASH + color] : 0;
		key ^= ((bits & (isWhite? Move.WQ_CASTLE : Move.BQ_CASTLE)) != 0)? hashBox[CASTLE_HASH + 2 * color] : 0;
		key ^= ((bits & Move.CAN_EP) != 0)? hashBox[ENPASSANT_HASH] : 0;
		key ^= hashBox[13 * EE64(move.to) + pieceType[move.index] + 6];

		if (move.getCastle() != 0) {
			final boolean left = (move.from - move.to > 0);
			final int castleFrom = move.to - (move.to & Move.EP_FILE) + (left? 0 : 7);
			final int castleI = pieceIndex(move.to + (left? 1 : -1), isWhite? Piece.WHITE_ROOK : Piece.BLACK_ROOK);

			key ^= hashBox[13 * EE64(piece[castleI]) + pieceType[castleI] + 6];
			key ^= hashBox[13 * EE64(castleFrom) + pieceType[castleI] + 6];

			square[piece[castleI]] = Piece.EMPTY;
			square[castleFrom] = pieceType[castleI];
			piece[castleI] = castleFrom;
			mScore += stm * locValue[Piece.ROOK][EE64(castleFrom)];
			mScore -= stm * locValue[Piece.ROOK][EE64(castleI)];
		} else if (move.getPromote() != 0) {
			pieceType[move.index] = Piece.PAWN * color;
		}

		key ^= hashBox[13 * EE64(move.from) + pieceType[move.index] + 6];

		piece[move.index] = move.from;
		mScore += stm * locValue[Math.abs(square[move.to])][EE64(move.to)];
		if (move.xindex == Piece.NONE) {
			square[move.to] = Piece.EMPTY;
		} else {
			if (move.getEnPassant()) {
				piece[move.xindex] = move.to + -16 * color;
				square[piece[move.xindex]] = Piece.PAWN * -color;
				square[move.to] = Piece.EMPTY;
			} else {
				piece[move.xindex] = move.to;
				square[move.to] = pieceType[move.xindex];
			}
			key ^= hashBox[13 * EE64(piece[move.xindex]) + pieceType[move.xindex] + 6];
			mScore += stm * locValue[Math.abs(pieceType[move.xindex])][EE64(piece[move.xindex])];
			mScore += stm * pieceValue[Math.abs(pieceType[move.xindex])];
		}
		square[move.from] = pieceType[move.index];

		mScore -= stm * locValue[Math.abs(square[move.from])][EE64(move.from)];
		key ^= hashBox[WTM_HASH];
		flags.bits = UndoFlags.bits;
		stm ^= -2;
		ply--;
	}

	@Override
	public int isMate()
	{
		final MoveList mList = getMoveList(stm, Move.MOVE_ALL);
	try {
		if (mList.size != 0)
			return Move.NOT_MATE;
		else if (inCheck(stm))
			return Move.CHECK_MATE;
		return Move.STALE_MATE;
	} finally {
		pool.put(mList);
	}
	}

	@Override
	public boolean validMove(final Move moveIn, final Move move)
	{
		if (moveIn.isNull() || moveIn.from == moveIn.to)
			return false;

		undoFlags.set(flags);
		move.set(moveIn);

		move.index = pieceIndex(move.from, square[move.from]);
		if (move.index == Piece.NONE)
			return false;
		else if (square[move.from] * stm < 0)
			return false;
		move.xindex = pieceIndex(move.to, square[move.to]);
		if (move.xindex != Piece.NONE && square[move.to] * stm > 0)
			return false;

		if (move.getCastle() != 0) {
			return validCastle(move, stm) == Move.VALID_MOVE;
		} else if (move.getEnPassant() && flags.canEnPassant() != 0) {
			return validEnPassant(move, stm) == Move.VALID_MOVE;
		} else if (isPromote(move, stm) && Math.abs(square[move.from]) == Piece.PAWN) {
			if (move.getPromote() == 0)
				move.setPromote(Piece.QUEEN);
		} else {
			move.flags = 0;
		}

		if (!fromTo(move.from, move.to))
			return false;

		boolean ret = true;

		make(move);
		// stm is opponent after make
		if (inCheck(stm ^ -2))
			ret = false;
		unmake(move, undoFlags);

		return ret;
	}

	private int validCastle(final Move move, final int color)
	{
		// can we castle on that side
		if (flags.canCastle(color) == 0 || move.getCastle() == 0)
			return Move.CANT_CASTLE;
		// can't castle while in check
		if (inCheck(color))
			return Move.CANT_CASTLE;

		final int king = (color == Piece.WHITE)? Piece.E1 : Piece.E8;

		// king side
		if (move.getCastle() == Move.CASTLE_KS && square[king + 1] == Piece.EMPTY && square[king + 2] == Piece.EMPTY &&
		!isAttacked(king + 1, color) && !isAttacked(king + 2, color) &&
		Math.abs(square[((color == Piece.WHITE)? Piece.H1:Piece.H8)]) == Piece.ROOK) {
			move.index = (color == Piece.WHITE)? 31 : 15;
			move.xindex = Piece.NONE;
			move.from = king;
			move.to = king + 2;
			return Move.VALID_MOVE;
		} else if (move.getCastle() == Move.CASTLE_QS && square[king - 1] == Piece.EMPTY && square[king - 2] == Piece.EMPTY &&
		square[king - 3] == Piece.EMPTY && !isAttacked(king - 1, color) && !isAttacked(king - 2, color) &&
		Math.abs(square[((color == Piece.WHITE)? Piece.A1:Piece.A8)]) == Piece.ROOK) {
			move.index = (color == Piece.WHITE)? 31 : 15;
			move.xindex = Piece.NONE;
			move.from = king;
			move.to = king - 2;
			return Move.VALID_MOVE;
		}
		return Move.CANT_CASTLE;
	}

	private int validEnPassant(final Move move, final int color)
	{
		undoFlags.set(flags);
		final int ep = flags.enPassantFile() + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
			ep_to = ep + ((color == Piece.WHITE)? 16 : -16);

		if (move.to == ep_to && Math.abs(ep - move.from) == 1) {
			move.index = pieceIndex(move.from, square[move.from]);
			move.xindex = pieceIndex(ep, square[ep]);
			move.setEnPassant();

			int ret = Move.VALID_MOVE;

			make(move);
			// stm is opponent after make
			if (inCheck(stm ^ -2))
				ret = Move.IN_CHECK;
			unmake(move, undoFlags);
			return ret;
		}
		return Move.INVALID_MOVEMENT;
	}

	@Override
	public Pair<Move,Integer> parseMove(String moveStr)
	{
		var move = newMove();
		if (!move.parse(moveStr))
			return new Pair<>(move, Move.INVALID_FORMAT);

		undoFlags.set(flags);
		final int color = getStm();

		// if castle flag is set, move must a castle to be valid
		if (move.getCastle() != 0)
			return new Pair<>(move, validCastle(move, color));

		move.index = pieceIndex(move.from, square[move.from]);

		if (move.index == Piece.NONE)
			return new Pair<>(move, Move.INVALID_MOVEMENT);

		switch (Math.abs(pieceType[move.index])) {
		case Piece.PAWN:
			// en passant
			if (flags.canEnPassant() != 0 && validEnPassant(move, color) == Move.VALID_MOVE)
				return new Pair<>(move, Move.VALID_MOVE);

			if (!isPromote(move, color)) {
				move.flags = 0;
				break;
			} else if (move.getPromote() == 0) {
				// manually set to queen if not specified
				move.setPromote(Piece.QUEEN);
			}
			break;
		case Piece.KING:
			// manual castling without proper O-O/O-O-O notation
			if (Math.abs(move.from - move.to) == 2) {
				move.setCastle((move.from > move.to)? Move.CASTLE_QS : Move.CASTLE_KS);
				return new Pair<>(move, validCastle(move, color));
			}
		default:
			// move can't be special, so clear flags
			move.flags = 0;
		}

		if (move.index == Piece.NONE)
			return new Pair<>(move, Move.NOPIECE_ERROR);
		else if (square[move.from] * color < 0)
			return new Pair<>(move, Move.DONT_OWN);
		move.xindex = pieceIndex(move.to, square[move.to]);
		if (move.xindex != Piece.NONE && square[move.to] * color > 0)
			return new Pair<>(move, Move.CAPTURE_OWN);

		if (!fromTo(move.from, move.to))
			return new Pair<>(move, Move.INVALID_MOVEMENT);

		int ret = Move.VALID_MOVE;

		make(move);
		// stm is opponent after make
		if (inCheck(stm ^ -2))
			ret = Move.IN_CHECK;
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
			pieceType[i] = Move.InitPieceType[i];
		}
		flags.reset();
	}

	@Override
	protected void setMaxPly()
	{
		var tPly = 0;
		for (var i = 0; i < 32; i++) {
			if (piece[i] == Piece.DEAD)
				tPly += 2;
			else if (piece[i] != InitRegPiece[i])
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
	protected boolean setPiece(final int loc, final int type)
	{
		var start = ((type < 0)? 0 : 16) + idxOffset[Math.abs(type)];
		var end = ((type < 0)? 0 : 16) + idxOffset[Math.abs(type) + 1];

		// try pieces in their initial location
		for (var i = start; i < end; i++) {
			if (InitRegPiece[i] == loc && piece[i] == Piece.DEAD) {
				piece[i] = loc;
				square[loc] = type;
				return true;
			}
		}
		// piece moved but not promoted
		for (var i = start; i < end; i++) {
			if (piece[i] == Piece.DEAD) {
				piece[i] = loc;
				square[loc] = type;
				return true;
			}
		}

		// piece might be a promote
		if (Math.abs(type) == Piece.PAWN || Math.abs(type) == Piece.KING)
			return false;

		var pStart = (type > 0)? 16:0;
		var pend = (type > 0)? 24:8;
		for (var i = pStart; i < pend; i++) {
			if (piece[i] == Piece.DEAD) {
				piece[i] = loc;
				pieceType[i] = type;
				square[loc] = type;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean inCheck(final int color)
	{
		var king = (color == Piece.WHITE)? 31:15;
		return isAttacked(piece[king], color);
	}

	@Override
	public int parseZFen_Specific(int n, String pos)
	{
		var st = pos.toCharArray();

		// parse castle rights
		var castle = 0;
		for (; st[n] != ':'; n++) {
			switch (st[n]) {
			case 'K':
				castle |= Move.WK_CASTLE;
				break;
			case 'Q':
				castle |= Move.WQ_CASTLE;
				break;
			case 'k':
				castle |= Move.BK_CASTLE;
				break;
			case 'q':
				castle |= Move.BQ_CASTLE;
				break;
			}
		}
		flags.setCastle(castle);

		// parse en passant
		n++;
		if (st[n] != ':') {
			var eps = (st[n++] - 'a');
			eps += 16 * (st[n++] - '1');
			flags.setEnPassant(eps & Move.EP_FILE);
		}
		n++;
		return n;
	}

	@Override
	protected void printZFen_Specific(StringBuilder fen)
	{
		// print castle rights
		if ((flags.bits & 0xf0) != 0) {
			if (flags.canKingCastle(Piece.WHITE) != 0)
				fen.append('K');
			if (flags.canQueenCastle(Piece.WHITE) != 0)
				fen.append('Q');
			if (flags.canKingCastle(Piece.BLACK) != 0)
				fen.append('k');
			if (flags.canQueenCastle(Piece.BLACK) != 0)
				fen.append('q');
		}
		fen.append(':');

		if (flags.canEnPassant() != 0) {
			fen.append((char) ('a' + flags.enPassantFile()));
			fen.append((ply % 2 != 0)? '3':'6');
		}
	}

	@Override
	protected int[] genAll_Pawn(int From, int[] list)
	{
		var next = 0;
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
		var next = 0;
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
		var next = 0;
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
	public boolean attackLine_Bishop(int From, int offset)
	{
		for (int to = From + offset, k = 1; ON_BOARD(to); to += offset, k++) {
			if (CAPTURE_MOVE(square[From], square[to])) {
				var to_type = Math.abs(square[to]);
				if (to_type == Piece.BISHOP || to_type == Piece.QUEEN) {
					return true;
				} else if (k == 1) {
					if (to_type == Piece.PAWN && square[From] * (to - From) > 0)
						return true;
					else
						return to_type == Piece.KING;
				}
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
				if (CAPTURE_MOVE(Color, to_piece)) {
					var to_type = Math.abs(to_piece);
					if (to_type == Piece.BISHOP || to_type == Piece.QUEEN) {
						return true;
					} else if (k == 1) {
						if (to_type == Piece.PAWN && Color * (to - From) > 0)
							return true;
						else if (to_type == Piece.KING)
							return true;
					}
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
		undoFlags.set(flags);

		for (int idx = start; idx >= end; idx--) {
			if (piece[idx] == Piece.DEAD)
				continue;

			final int[] loc;
			switch (moveType) {
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

				if (Math.abs(pieceType[idx]) == Piece.PAWN && isPromote(item.move, color)) {
					item.move.setPromote(Piece.QUEEN);

					make(item.move);
					if (inCheckMove(item.move, color, stmCk)) {
						unmake(item.move, undoFlags);
						continue;
					}
					unmake(item.move, undoFlags);

					data.add(item);
					for (int i = Piece.ROOK; i > Piece.PAWN; i--) {
						item.move.setPromote(i);

						make(item.move);
						item.check = inCheckMove(item.move, color ^ -2, false);
						item.score = eval();
						unmake(item.move, undoFlags);

						data.add(item);
					}
				} else {
					// must clear move flags
					item.move.flags = 0;
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
	}

	private void getCastleMoveList(final MoveList data, final int color)
	{
		// can't castle while in check
		if (inCheck(color))
			return;

		final int king = (color == Piece.WHITE)? Piece.E1 : Piece.E8,
			kIndex = (color == Piece.WHITE)? 31 : 15;

		// King Side
		if (flags.canKingCastle(color) != 0 && square[king + 1] == Piece.EMPTY && square[king + 2] == Piece.EMPTY &&
		!isAttacked(king + 1, color) && !isAttacked(king + 2, color) &&
		Math.abs(square[((color == Piece.WHITE)? Piece.H1:Piece.H8)]) == Piece.ROOK) {
			item.move.xindex = Piece.NONE;
			item.move.to = king + 2;
			item.move.from = king;
			item.move.index = kIndex;
			item.move.setCastle(Move.CASTLE_KS);
			item.score = eval();
			item.check = false;

			data.add(item);
		}
		// Queen Side
		if (flags.canQueenCastle(color) != 0 && square[king - 1] == Piece.EMPTY && square[king - 2] == Piece.EMPTY &&
		square[king - 3] == Piece.EMPTY && !isAttacked(king - 1, color) && !isAttacked(king - 2, color) &&
		Math.abs(square[((color == Piece.WHITE)? Piece.A1:Piece.A8)]) == Piece.ROOK) {
			item.move.xindex = Piece.NONE;
			item.move.to = king - 2;
			item.move.from = king;
			item.move.index = kIndex;
			item.move.setCastle(Move.CASTLE_QS);
			item.score = eval();
			item.check = false;

			data.add(item);
		}
	}

	private void getEnPassantMoveList(final MoveList data, final int color)
	{
		if (flags.canEnPassant() == 0)
			return;

		final int eps_file = flags.enPassantFile(),
			eps = eps_file + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
			your_pawn = (color == Piece.WHITE)? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
			opp_pawn = -your_pawn;
		undoFlags.set(flags);

		// en passant to left
		if (eps_file != 7 && square[eps + 1] == your_pawn) {
			item.move.xindex = pieceIndex(eps, opp_pawn);
			item.move.to = eps + 16 * color;
			item.move.from = eps + 1;
			item.move.index = pieceIndex(eps + 1, your_pawn);
			item.move.setEnPassant();

			make(item.move);
			if (!inCheck(color)) {
				item.check = inCheck(color ^ -2);
				item.score = eval();
				data.add(item);
			}
			unmake(item.move, undoFlags);
		}
		// en passant to right
		if (eps_file != 0 && square[eps - 1] == your_pawn) {
			item.move.xindex = pieceIndex(eps, opp_pawn);
			item.move.to = eps + 16 * color;
			item.move.from = eps - 1;
			item.move.index = pieceIndex(eps - 1, your_pawn);
			item.move.setEnPassant();

			make(item.move);
			if (!inCheck(color)) {
				item.check = inCheck(color ^ -2);
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
		case Move.MOVE_ALL:
		default:
			getMoveList(data, color, moveType);
			getCastleMoveList(data, color);
			getEnPassantMoveList(data, color);
			break;
		case Move.MOVE_CAPTURE:
			getMoveList(data, color, moveType);
			getEnPassantMoveList(data, color);
			break;
		case Move.MOVE_MOVE:
			getMoveList(data, color, moveType);
			getCastleMoveList(data, color);
			break;
		}
		return data;
	}
}
