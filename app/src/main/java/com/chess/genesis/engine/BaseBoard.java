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

import java.util.*;

public abstract class BaseBoard implements Board
{
	static final int ZBOX_SIZE = 838;
	static final int WTM_HASH = 837;
	static final int ENPASSANT_HASH = 834;
	static final int CASTLE_HASH = 834;
	static final int HOLD_START = 768;

	protected static final int[] pieceValue =
		{0, 224, 336, 560, 896, 1456, 0};

	protected final static int[] idxOffset = {-1, 0, 8, 10, 12, 14, 15, 16};

	static final int[] stype = {
		Piece.EMPTY,		Piece.EMPTY,		Piece.BLACK_KING,	Piece.WHITE_BISHOP,
		Piece.EMPTY,		Piece.BLACK_KNIGHT,	Piece.EMPTY,		Piece.BLACK_PAWN,
		Piece.BLACK_QUEEN,	Piece.BLACK_ROOK,	Piece.EMPTY,		Piece.EMPTY,
		Piece.WHITE_KING,	Piece.EMPTY,		Piece.BLACK_BISHOP,	Piece.WHITE_KNIGHT,
		Piece.EMPTY,		Piece.WHITE_PAWN,	Piece.WHITE_QUEEN,	Piece.WHITE_ROOK};

	static final int[] KNIGHT_OFFSETS = {33, 31, 18, 14, -33, -31, -18, -14};
	static final int[] BISHOP_OFFSETS = {17, 15, -17, -15};
	static final int[] ROOK_OFFSETS = {16, 1, -16, -1};
	static final int[] QUEEN_OFFSETS = {17, 16, 15, 1, -17, -16, -15, -1}; // Pawn: even=capture

	protected static int[] getOffset(int piece)
	{
		switch (Math.abs(piece)) {
		case Piece.KNIGHT:
			return KNIGHT_OFFSETS;
		case Piece.BISHOP:
			return BISHOP_OFFSETS;
		case Piece.ROOK:
			return ROOK_OFFSETS;
		default:
			return QUEEN_OFFSETS;
		}
	}

	private final int[] list = new int[28];

	int[] square;
	int[] piece;
	int[] pieceType;
	int ply;
	int stm;
	long key;
	int mScore;

	abstract boolean setPiece(int loc, int type);
	abstract void parseReset();
	abstract void setMaxPly();
	abstract MoveListPool movePool();

	abstract int[] genAll_Pawn(int From, int[] list);
	abstract int[] genCapture_Pawn(int From, int[] list);
	abstract int[] genMove_Pawn(int From, int[] list);

	abstract boolean fromTo_Pawn(int From, int To);
	abstract boolean isAttacked_Bishop(int From, int Color);
	abstract boolean attackLine_Bishop(int From, int offset);

	abstract void printZFen_Specific(StringBuilder fen);
	abstract int parseZFen_Specific(int n, String pos);

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

	private static boolean ANY_MOVE(final int A, final int B)
	{
		return (A * B <= 0);
	}

	public static int EE64(final int x)
	{
		return ((x & 7) + x) >> 1;
	}

	public static int EE64F(final int x)
	{
		return ((7 - (x >> 4)) << 3) + (x & 7);
	}

	public static int SF88(final int x)
	{
		return (x & ~7) + x;
	}

	public static int SFF88(final int x)
	{
		return ((7 - (x >> 3)) << 4) + (x & 7);
	}

	@Override
	public int pieceLoc(int index)
	{
		return piece[index];
	}

	@Override
	public int pieceType(int index)
	{
		return pieceType[index];
	}

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
	public int kingIndex(int color)
	{
		return piece[Piece.WHITE == color ? 31 : 15];
	}

	@Override
	public int[] getPieceCounts(int Loc)
	{
		var counts = new int[13];
		for (var i = 0; i < 32; i++) {
			if (piece[i] == Loc)
				counts[pieceType[i] + 6]++;
		}
		return counts;
	}

	@Override
	public int[] getBoardArray()
	{
		return square;
	}

	boolean inCheckMove(Move move, int color, boolean stmCk)
	{
		var king = (color == Piece.WHITE)? 31:15;
		if (stmCk || move.index == king)
			return inCheck(color);
		return attackLine(piece[king], move.from) || attackLine(piece[king], move.to);
	}

	@Override
	public int isMate()
	{
		var mList = getMoveList(stm, Move.MOVE_ALL);
		try {
			if (mList.size != 0)
				return Move.NOT_MATE;
			else if (inCheck(stm))
				return Move.CHECK_MATE;
			return Move.STALE_MATE;
		} finally {
			movePool().put(mList);
		}
	}

	@Override
	public int eval()
	{
		return (stm == Piece.WHITE)? -mScore : mScore;
	}

	int[] genAll(int From)
	{
		int next = 0;
		var from_piece = square[From];
		var type = Math.abs(from_piece);

		switch (type) {
		case Piece.PAWN:
			return genAll_Pawn(From, list);
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (var diff : getOffset(from_piece)) {
				for (var to = From + diff; ON_BOARD(to); to += diff) {
					var to_piece = square[to];
					if (ANY_MOVE(from_piece, to_piece)) {
						list[next++] = to;
					} else if (to_piece != Piece.EMPTY) {
						break;
					}
				}
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (var diff : getOffset(from_piece)) {
				var to = From + diff;
				if (ON_BOARD(to) && ANY_MOVE(from_piece, square[to]))
					list[next++] = to;
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	int[] genCapture(int From)
	{
		int next = 0;
		var from_piece = square[From];
		var type = Math.abs(from_piece);

		switch (type) {
		case Piece.PAWN:
			return genCapture_Pawn(From, list);
		case Piece.KNIGHT:
		case Piece.KING:
			for (var diff : getOffset(from_piece)) {
				var to = From + diff;
				if (ON_BOARD(to) && CAPTURE_MOVE(from_piece, square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (var diff : getOffset(from_piece)) {
				for (var to = From + diff; ON_BOARD(to); to += diff) {
					if (CAPTURE_MOVE(from_piece, square[to])) {
						list[next++] = to;
						break;
					}
				}
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	int[] genMove(int From)
	{
		int next = 0;
		var from_piece = square[From];
		var type = Math.abs(from_piece);

		switch (type) {
		case Piece.PAWN:
			return genMove_Pawn(From, list);
		case Piece.KNIGHT:
		case Piece.KING:
			for (var diff : getOffset(from_piece)) {
				var to = From + diff;
				if (ON_BOARD(to) && square[to] == Piece.EMPTY)
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (var diff : getOffset(from_piece)) {
				for (var to = From + diff; ON_BOARD(to); to += diff) {
					if (square[to] == Piece.EMPTY)
						list[next++] = to;
					else
						break;
				}
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	boolean fromTo(int From, int To)
	{
		if (OFF_BOARD(From | To) || OWN_PIECE(square[From], square[To]))
			return false;

		var from_piece = square[From];
		var type = Math.abs(from_piece);
		var diff = Math.abs(To - From);

		switch (type) {
		case Piece.PAWN:
			return fromTo_Pawn(From, To);
		case Piece.KNIGHT:
		case Piece.KING:
			return IntArray.contains(getOffset(from_piece), To - From);
		case Piece.QUEEN:
			if (diff < 8) {
				return fromTo_Diff(From, To, To > From ? 1 : -1);
			} else if (diff % 16 == 0) {
				return fromTo_Diff(From, To, To > From ? 16 : -16);
			} else if (diff % 15 == 0) {
				return fromTo_Diff(From, To, To > From ? 15 : -15);
			} else if (diff % 17 == 0) {
				return fromTo_Diff(From, To, To > From ? 17 : -17);
			}
			return false;
		case Piece.BISHOP:
			if (diff % 15 == 0) {
				return fromTo_Diff(From, To, To > From ? 15 : -15);
			} else if (diff % 17 == 0) {
				return fromTo_Diff(From, To, To > From ? 17 : -17);
			}
			return false;
		case Piece.ROOK:
			if (diff < 8) {
				return fromTo_Diff(From, To, To > From ? 1 : -1);
			} else if (diff % 16 == 0) {
				return fromTo_Diff(From, To, To > From ? 16 : -16);
			}
			return false;
		}
		return false;
	}

	boolean fromTo_Diff(int From, int To, int offset)
	{
		for (int k = From + offset; ON_BOARD(k); k += offset) {
			if (k == To)
				return true;
			else if (square[k] != Piece.EMPTY)
				return false;
		}
		return false;
	}

	boolean isAttacked(int From, int Color)
	{
		if (isAttacked_Bishop(From, Color)) {
			return true;
		}

		// ROOK
		for (int diff : ROOK_OFFSETS) {
			for (int to = From + diff, k = 1; ON_BOARD(to); to += diff, k++) {
				var to_piece = square[to];
				var to_type = Math.abs(to_piece);
				if (CAPTURE_MOVE(Color, to_piece)) {
					if (to_type == Piece.ROOK || to_type == Piece.QUEEN || (k == 1 && to_type == Piece.KING)) {
						return true;
					}
					break;
				} else if (OWN_PIECE(Color, to_piece)) {
					break;
				}
			}
		}
		// KNIGHT
		for (var diff : KNIGHT_OFFSETS) {
			var to = From + diff;
			if (ON_BOARD(to)) {
				var to_piece = square[to];
				var to_type = Math.abs(to_piece);
				if (CAPTURE_MOVE(Color, to_piece) && to_type == Piece.KNIGHT)
					return true;
			}
		}
		return false;
	}

	boolean attackLine(int From, int To)
	{
		if (OFF_BOARD(From | To))
			return false;

		var diff = Math.abs(To - From);
		if (diff < 8) {
			return attackLine_Rook(From, To > From ? 1 : -1);
		} else if (diff % 15 == 0) {
			return attackLine_Bishop(From, To > From ? 15 : -15);
		} else if (diff % 17 == 0) {
			return attackLine_Bishop(From, To > From ? 17 : -17);
		} else if (diff % 16 == 0) {
			return attackLine_Rook(From, To > From ? 16 : -16);
		}
		return Math.abs(square[To]) == Piece.KNIGHT && IntArray.contains(KNIGHT_OFFSETS, diff);
	}

	boolean attackLine_Rook(int From, int offset)
	{
		for (int to = From + offset, k = 1; ON_BOARD(to); to += offset, k++) {
			var to_piece = square[to];
			var to_type = Math.abs(to_piece);
			if (CAPTURE_MOVE(square[From], to_piece)) {
				if (k == 1 && to_type == Piece.KING)
					return true;
				return to_type == Piece.ROOK || to_type == Piece.QUEEN;
			} else if (OWN_PIECE(square[From], to_piece)) {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean parseZFen(String pos)
	{
		parseReset();
		final char[] st = pos.toCharArray();

		// index counter for st
		int n = 0;

		// parse board
		StringBuilder num = new StringBuilder();
		boolean act = false;
		for (int loc = 0; true; n++) {
			if (Character.isDigit(st[n])) {
				num.append(st[n]);
				act = true;
			} else if (Character.isLetter(st[n])) {
				if (act) {
					loc += Integer.parseInt(num.toString());
					num = new StringBuilder();
					act = false;
				}
				if (!setPiece(SFF88(loc), stype[st[n] % 21]))
					return false;
				loc++;
			} else if (st[n] == ':') {
				n++;
				break;
			} else {
				return false;
			}
		}

		n = parseZFen_Specific(n, pos);
		if (n < 0)
			return false;

		// parse half-ply
		num = new StringBuilder();
		while (n < st.length && Character.isDigit(st[n])) {
			num.append(st[n]);
			n++;
		}
		ply = Integer.parseInt(num.toString());
		stm = ply % 2 == 0 ? Piece.WHITE : Piece.BLACK;

		setMaxPly();

		// check if color not on move is in check
		return !inCheck(stm ^ -2);
	}

	@Override
	public String printZFen()
	{
		var fen = new StringBuilder();
		for (int i = 0, empty = 0; i < 64; i++) {
			// convert coordinate system
			final int n = SFF88(i);
			if (square[n] == Piece.EMPTY) {
				empty++;
				continue;
			} else if (empty != 0) {
				fen.append(empty);
			}

			fen.append(Move.PIECE_SYM[square[n] + 6]);
			empty = 0;
		}
		fen.append(':');
		printZFen_Specific(fen);
		fen.append(':');
		fen.append(ply);

		return fen.toString();
	}
}
