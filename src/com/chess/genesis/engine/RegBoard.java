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

package com.chess.genesis;

class RegBoard extends RegPosition implements Board
{
	private static final int[][] regLocValue = {
		{0, 0, 0, 0, 0, 0, 0, 0,
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

	private static final int[] regPieceValue =
		{0, 224, 336, 560, 896, 1456, 0};

	public static final int ZBOX_SIZE = 838;
	public static final int WTM_HASH = 837;
	public static final int HOLD_START = 768;
	public static final int HOLD_END = 781;
	public static final int ENPASSANT_HASH = 834;
	public static final int CASTLE_HASH = 834;

	public static long[] hashBox = new long[ZBOX_SIZE];
	public static long startHash;

	private long key;
	private int mscore;

	public RegBoard()
	{
		reset();
	}

	public RegBoard(final RegBoard board)
	{
		square = IntArray.clone(board.square);
		piece = IntArray.clone(board.piece);
		piecetype = IntArray.clone(board.piecetype);

		flags = new MoveFlags(board.flags);
		ply = board.ply;
		stm = board.stm;
		key = board.key;
	}

	public final void reset()
	{
		piece = IntArray.clone(InitRegPiece);
		piecetype = IntArray.clone(Move.InitPieceType);
		for (int i = 0; i < 32; i++)
			square[piece[i]] = piecetype[i];

		key = startHash;
		stm = Piece.WHITE;
		ply = 0;
		flags.reset();
	}

	public int Piece(final int index)
	{
		return piece[index];
	}

	public int PieceType(final int index)
	{
		return piecetype[index];
	}

	// Do Not call the following functions!
	public void unmake(final GenMove move){}
	public void make(final GenMove move){}
	public int validMove(final GenMove move){ return Move.INVALID_MOVEMENT; }
	public boolean validMove(final GenMove moveIn, final GenMove move) { return false; }
	// ------

	public int getStm()
	{
		return stm;
	}

	public int getPly()
	{
		return ply;
	}

	public long hash()
	{
		return key;
	}

	public int kingIndex(final int color)
	{
		return (Piece.WHITE == color)? piece[31] : piece[15];
	}

	public MoveFlags getMoveFlags()
	{
		return new MoveFlags(flags);
	}

	public int[] getPieceCounts(final int Loc)
	{
		final int[] counts = new int[13];

		for (int i = 0; i < 32; i++) {
			if (piece[i] == Loc)
				counts[piecetype[i] + 6]++;
		}
		return counts;
	}

	public int[] getBoardArray()
	{
		return square;
	}

	private int pieceIndex(final int loc, final int type)
	{
		final int start = (type > 0)? 16:0, end = (type > 0)? 32:16;

		for (int i = start; i < end; i++)
			if (piece[i] == loc && piecetype[i] == type)
				return i;
		return Piece.NONE;
	}

	private boolean isPromote(final RegMove move, final int color)
	{
		return (color == Piece.WHITE)? (move.to >= Piece.A8) : (move.to <= Piece.H1);
	}

	public void make(final RegMove move)
	{
		final boolean isWhite = (move.index > 15);
		final int color = isWhite? Piece.WHITE : Piece.BLACK;

		key ^= hashBox[13 * EE64(move.from) + piecetype[move.index] + 6];

		if (move.getCastle() != 0) {
			final boolean left = (move.getCastle() == Move.CASTLE_QS);
			final int castleTo = move.to + (left? 1 : -1);
			final int castleI = pieceIndex(move.to - (move.to & Move.EP_FILE) + (left? 0 : 7), color * Piece.ROOK);

			key ^= hashBox[13 * EE64(piece[castleI]) + piecetype[castleI] + 6];
			key ^= hashBox[13 * EE64(castleTo) + piecetype[castleI] + 6];
			if (flags.canKingCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color];
			if (flags.canQueenCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color * 2];

			square[castleTo] = piecetype[castleI];
			square[piece[castleI]] = Piece.EMPTY;
			piece[castleI] = castleTo;
			mscore += stm * regLocValue[Piece.ROOK][EE64(castleTo)];
			mscore -= stm * regLocValue[Piece.ROOK][EE64(castleI)];
			flags.clearCastle(color);
		} else if (Math.abs(piecetype[move.index]) == Piece.ROOK) {
			if (move.from == (isWhite? Piece.H1:Piece.H8) && flags.canKingCastle(color) != 0) {
				flags.clearKingCastle(color);
				key ^= hashBox[CASTLE_HASH + color];
			} else if (move.from == (isWhite? Piece.A1:Piece.A8) && flags.canQueenCastle(color) != 0) {
				flags.clearQueenCastle(color);
				key ^= hashBox[CASTLE_HASH + color * 2];
			}
		} else if (Math.abs(piecetype[move.index]) == Piece.KING && flags.canCastle(color) != 0) {
			if (flags.canKingCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color];
			if (flags.canQueenCastle(color) != 0)
				key ^= hashBox[CASTLE_HASH + color * 2];

			flags.clearCastle(color);
		} else if (move.getPromote() != 0) {
			piecetype[move.index] = move.getPromote() * color;
		}
		key ^= hashBox[13 * EE64(move.to) + piecetype[move.index] + 6];

		if (flags.canEnPassant() != 0) {
			flags.clearEnPassant();
			key ^= hashBox[ENPASSANT_HASH];
		}

		// update board information
		square[move.to] = piecetype[move.index];
		mscore += stm * regLocValue[Math.abs(square[move.to])][EE64F(move.to)];
		mscore -= stm * regLocValue[Math.abs(square[move.from])][EE64F(move.from)];
		square[move.from] = Piece.EMPTY;
		// update piece information
		piece[move.index] = move.to;
		if (move.xindex != Piece.NONE) {
			key ^= hashBox[13 * EE64(piece[move.xindex]) + piecetype[move.xindex] + 6];
			mscore += stm * regLocValue[Math.abs(piecetype[move.xindex])][EE64F(piece[move.xindex])];
			mscore += stm * regPieceValue[Math.abs(piecetype[move.xindex])];

			if (move.getEnPassant())
				square[piece[move.xindex]] = Piece.EMPTY;
			piece[move.xindex] = Piece.DEAD;
		} else if (Math.abs(piecetype[move.index]) == Piece.PAWN && Math.abs(move.to - move.from) == 32) {
			flags.setEnPassant(move.to & Move.EP_FILE);
			key ^= hashBox[ENPASSANT_HASH];
		}

		key ^= hashBox[WTM_HASH];
		stm ^= -2;
		ply++;
	}

	public void unmake(final RegMove move, final MoveFlags undoFlags)
	{
		final boolean isWhite = (move.index > 15);
		final int color = isWhite? Piece.WHITE : Piece.BLACK,
			bits = flags.bits ^ undoFlags.bits;

		key ^= ((bits & (isWhite? Move.WK_CASTLE : Move.BK_CASTLE)) != 0)? hashBox[CASTLE_HASH + color] : 0;
		key ^= ((bits & (isWhite? Move.WQ_CASTLE : Move.BQ_CASTLE)) != 0)? hashBox[CASTLE_HASH + 2 * color] : 0;
		key ^= ((bits & Move.CAN_EP) != 0)? hashBox[ENPASSANT_HASH] : 0;
		key ^= hashBox[13 * EE64(move.to) + piecetype[move.index] + 6];

		if (move.getCastle() != 0) {
			final boolean left = (move.from - move.to > 0);
			final int castleFrom = move.to - (move.to & Move.EP_FILE) + (left? 0 : 7);
			final int castleI = pieceIndex(move.to + (left? 1 : -1), isWhite? Piece.WHITE_ROOK : Piece.BLACK_ROOK);

			key ^= hashBox[13 * EE64(piece[castleI]) + piecetype[castleI] + 6];
			key ^= hashBox[13 * EE64(castleFrom) + piecetype[castleI] + 6];

			square[piece[castleI]] = Piece.EMPTY;
			square[castleFrom] = piecetype[castleI];
			piece[castleI] = castleFrom;
			mscore += stm * regLocValue[Piece.ROOK][EE64(castleFrom)];
			mscore -= stm * regLocValue[Piece.ROOK][EE64(castleI)];
		} else if (move.getPromote() != 0) {
			piecetype[move.index] = Piece.PAWN * color;
		}

		key ^= hashBox[13 * EE64(move.from) + piecetype[move.index] + 6];

		piece[move.index] = move.from;
		mscore += stm * regLocValue[Math.abs(square[move.to])][EE64F(move.to)];
		if (move.xindex == Piece.NONE) {
			square[move.to] = Piece.EMPTY;
		} else {
			if (move.getEnPassant()) {
				piece[move.xindex] = move.to + -16 * color;
				square[piece[move.xindex]] = Piece.PAWN * -color;
				square[move.to] = Piece.EMPTY;
			} else {
				piece[move.xindex] = move.to;
				square[move.to] = piecetype[move.xindex];
			}
			key ^= hashBox[13 * EE64(piece[move.xindex]) + piecetype[move.xindex] + 6];
			mscore += stm * regLocValue[Math.abs(piecetype[move.xindex])][EE64F(piece[move.xindex])];
			mscore += stm * regPieceValue[Math.abs(piecetype[move.xindex])];
		}
		square[move.from] = piecetype[move.index];

		mscore -= stm * regLocValue[Math.abs(square[move.from])][EE64F(move.from)];
		key ^= hashBox[WTM_HASH];
		flags.bits = undoFlags.bits;
		stm ^= -2;
		ply--;
	}

	private boolean incheckMove(final RegMove move, final int color, final boolean stmCk)
	{
		final int king = (color == Piece.WHITE)? 31:15;
		if (stmCk || move.index == king)
			return incheck(color);
		else
			return (attackLine(piece[king], move.from) || attackLine(piece[king], move.to));
	}

	public int isMate()
	{
		if (anyMoves(stm))
			return Move.NOT_MATE;
		else if (incheck(stm))
			return Move.CHECK_MATE;
		else
			return Move.STALE_MATE;
	}

	public boolean validMove(final RegMove moveIn, final RegMove move)
	{
		if (moveIn.from == moveIn.to)
			return false;

		final MoveFlags undoFlags = new MoveFlags(flags);
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

		if (!fromto(move.from, move.to))
			return false;

		boolean ret = true;

		make(move);
		// stm is opponent after make
		if (incheck(stm ^ -2))
			ret = false;
		unmake(move, undoFlags);

		return ret;
	}

	private int validCastle(final RegMove move, final int color)
	{
		// can we castle on that side
		if (flags.canCastle(color) == 0 || move.getCastle() == 0)
			return Move.CANT_CASTLE;
		// can't castle while in check
		if (incheck(color))
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

	private int validEnPassant(final RegMove move, final int color)
	{
		final MoveFlags undoFlags = new MoveFlags(flags);
		final int ep = flags.enPassantFile() + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
			ep_to = ep + ((color == Piece.WHITE)? 16 : -16);

		if (move.to == ep_to && Math.abs(ep - move.from) == 1) {
			move.index = pieceIndex(move.from, square[move.from]);
			move.xindex = pieceIndex(ep, square[ep]);
			move.setEnPassant();

			int ret = Move.VALID_MOVE;

			make(move);
			// stm is opponent after make
			if (incheck(stm ^ -2))
				ret = Move.IN_CHECK;
			unmake(move, undoFlags);
			return ret;
		}
		return Move.INVALID_MOVEMENT;
	}

	public int validMove(final RegMove move)
	{
		final MoveFlags undoFlags = new MoveFlags(flags);
		final int color = getStm();

		// if castle flag is set, move must a castle to be valid
		if (move.getCastle() != 0)
			return validCastle(move, color);

		move.index = pieceIndex(move.from, square[move.from]);

		if (move.index == Piece.NONE)
			return Move.INVALID_MOVEMENT;

		switch (Math.abs(piecetype[move.index])) {
		case Piece.PAWN:
			// en passant
			if (flags.canEnPassant() != 0 && validEnPassant(move, color) == Move.VALID_MOVE)
				return Move.VALID_MOVE;

			if (!isPromote(move, color)) {
				move.flags = 0;
				break;
			} else if (move.getPromote() == 0) {
				// manualy set to queen if not specified
				move.setPromote(Piece.QUEEN);
			}
			break;
		case Piece.KING:
			// manual castling without proper O-O/O-O-O notation
			if (Math.abs(move.from - move.to) == 2) {
				move.setCastle((move.from > move.to)? Move.CASTLE_QS : Move.CASTLE_KS);
				return validCastle(move, color);
			}
		default:
			// move can't be special, so clear flags
			move.flags = 0;
		}

		if (move.index == Piece.NONE)
			return Move.NOPIECE_ERROR;
		else if (square[move.from] * color < 0)
			return Move.DONT_OWN;
		move.xindex = pieceIndex(move.to, square[move.to]);
		if (move.xindex != Piece.NONE && square[move.to] * color > 0)
			return Move.CAPTURE_OWN;

		if (!fromto(move.from, move.to))
			return Move.INVALID_MOVEMENT;

		int ret = Move.VALID_MOVE;

		make(move);
		// stm is opponent after make
		if (incheck(stm ^ -2))
			ret = Move.IN_CHECK;
		unmake(move, undoFlags);

		return ret;
	}

	public int eval()
	{
		return (stm == Piece.WHITE)? -mscore : mscore;
	}

	public boolean anyMoves(final int color)
	{
		return (getMoveList(color, Move.MOVE_ALL).size != 0);
	}

	private void getMoveList(final RegMoveList data, final int color, final int movetype)
	{
		final boolean stmCk = incheck(color);
		final int start = (color == Piece.WHITE)? 31:15, end = (color == Piece.WHITE)? 16:0;
		final MoveFlags undoFlags = new MoveFlags(flags);

		for (int idx = start; idx >= end; idx--) {
			if (piece[idx] == Piece.DEAD)
				continue;

			final int[] loc;
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
				final RegMoveNode item = new RegMoveNode();
				item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
				item.move.to = loc[n];
				item.move.from = piece[idx];
				item.move.index = idx;

				if (Math.abs(piecetype[idx]) == Piece.PAWN && isPromote(item.move, color)) {
					item.move.setPromote(Piece.QUEEN);

					make(item.move);
					if (incheckMove(item.move, color, stmCk)) {
						unmake(item.move, undoFlags);
						continue;
					}
					unmake(item.move, undoFlags);

					data.list[data.size++] = item;
					for (int i = Piece.ROOK; i > Piece.PAWN; i--) {
						item.move.setPromote(i);

						make(item.move);
						item.check = incheckMove(item.move, color ^ -2, false);
						item.score = eval();
						unmake(item.move, undoFlags);

						data.list[data.size++] = new RegMoveNode(item);
					}
				} else {
					make(item.move);
					if (!incheckMove(item.move, color, stmCk)) {
						item.check = incheckMove(item.move, color ^ -2, false);
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move, undoFlags);
				}
			}
		}
	}

	private void getCastleMoveList(final RegMoveList data, final int color)
	{
		// can't castle while in check
		if (incheck(color))
			return;

		final int king = (color == Piece.WHITE)? Piece.E1 : Piece.E8,
			kindex = (color == Piece.WHITE)? 31 : 15;

		// King Side
		if (flags.canKingCastle(color) != 0 && square[king + 1] == Piece.EMPTY && square[king + 2] == Piece.EMPTY &&
		!isAttacked(king + 1, color) && !isAttacked(king + 2, color) &&
		Math.abs(square[((color == Piece.WHITE)? Piece.H1:Piece.H8)]) == Piece.ROOK) {
			final RegMoveNode item = new RegMoveNode();

			item.move.xindex = Piece.NONE;
			item.move.to = king + 2;
			item.move.from = king;
			item.move.index = kindex;
			item.move.setCastle(Move.CASTLE_KS);
			item.score = eval();
			item.check = false;

			data.list[data.size++] = item;
		}
		// Queen Side
		if (flags.canQueenCastle(color) != 0 && square[king - 1] == Piece.EMPTY && square[king - 2] == Piece.EMPTY &&
		square[king - 3] == Piece.EMPTY && !isAttacked(king - 1, color) && !isAttacked(king - 2, color) &&
		Math.abs(square[((color == Piece.WHITE)? Piece.A1:Piece.A8)]) == Piece.ROOK) {
			final RegMoveNode item = new RegMoveNode();

			item.move.xindex = Piece.NONE;
			item.move.to = king - 2;
			item.move.from = king;
			item.move.index = kindex;
			item.move.setCastle(Move.CASTLE_QS);
			item.score = eval();
			item.check = false;

			data.list[data.size++] = item;
		}
	}

	private void getEnPassantMoveList(final RegMoveList data, final int color)
	{
		if (flags.canEnPassant() == 0)
			return;

		final int eps_file = flags.enPassantFile(),
			eps = eps_file + ((color == Piece.WHITE)? Piece.A5 : Piece.A4),
			your_pawn = (color == Piece.WHITE)? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
			opp_pawn = -your_pawn;
		final MoveFlags undoFlags = new MoveFlags(flags);

		// en passant to left
		if (eps_file != 7 && square[eps + 1] == your_pawn) {
			final RegMoveNode item = new RegMoveNode();
			item.move.xindex = pieceIndex(eps, opp_pawn);
			item.move.to = eps + 16 * color;
			item.move.from = eps + 1;
			item.move.index = pieceIndex(eps + 1, your_pawn);
			item.move.setEnPassant();

			make(item.move);
			if (!incheck(color)) {
				item.check = incheck(color ^ -2);
				item.score = eval();
				data.list[data.size++] = item;
			}
			unmake(item.move, undoFlags);
		}
		// en passant to right
		if (eps_file != 0 && square[eps - 1] == your_pawn) {
			final RegMoveNode item = new RegMoveNode();
			item.move.xindex = pieceIndex(eps, opp_pawn);
			item.move.to = eps + 16 * color;
			item.move.from = eps - 1;
			item.move.index = pieceIndex(eps - 1, your_pawn);
			item.move.setEnPassant();

			make(item.move);
			if (!incheck(color)) {
				item.check = incheck(color ^ -2);
				item.score = eval();
				data.list[data.size++] = item;
			}
			unmake(item.move, undoFlags);
		}
	}

	public RegMoveList getMoveList(final int color, final int movetype)
	{
		final RegMoveList data = new RegMoveList();
		data.size = 0;

		switch (movetype) {
		case Move.MOVE_ALL:
		default:
			getMoveList(data, color, movetype);
			getCastleMoveList(data, color);
			getEnPassantMoveList(data, color);
			break;
		case Move.MOVE_CAPTURE:
			getMoveList(data, color, movetype);
			getEnPassantMoveList(data, color);
			break;
		case Move.MOVE_MOVE:
			getMoveList(data, color, movetype);
			getCastleMoveList(data, color);
			break;
		}
		return data;
	}
}
