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

public abstract class BaseBoard
{
	static final int[] stype = {
		Piece.EMPTY,		Piece.EMPTY,		Piece.BLACK_KING,	Piece.WHITE_BISHOP,
		Piece.EMPTY,		Piece.BLACK_KNIGHT,	Piece.EMPTY,		Piece.BLACK_PAWN,
		Piece.BLACK_QUEEN,	Piece.BLACK_ROOK,	Piece.EMPTY,		Piece.EMPTY,
		Piece.WHITE_KING,	Piece.EMPTY,		Piece.BLACK_BISHOP,	Piece.WHITE_KNIGHT,
		Piece.EMPTY,		Piece.WHITE_PAWN,	Piece.WHITE_QUEEN,	Piece.WHITE_ROOK};

	static final int[][] offsets = {
		{ 0,  0,   0,   0,   0,   0,   0,   0, 0, 0},
		{17, 16,  15,   1, -17, -16, -15,  -1, 0, 0}, // Pawn: even=capture
		{33, 31,  18,  14, -33, -31, -18, -14, 0, 0}, // Knight
		{17, 15, -17, -15,   0,   0,   0,   0, 0, 0}, // Bishop
		{16,  1, -16,  -1,   0,   0,   0,   0, 0, 0}, // Rook
		{17, 16,  15,   1, -17, -16, -15,  -1, 0, 0}, // Queen
		{17, 16,  15,   1, -17, -16, -15,  -1, 0, 0} }; // King

	int[] square;
	int[] piece;
	int[] piecetype;
	int ply;
	int stm;

	protected abstract boolean attackLine_Bishop(final DistDB db, final int From, final int To);
	protected abstract boolean setPiece(final int loc, final int type);
	protected abstract void parseReset();

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

	public static boolean NOT_CAPTURE(final int A, final int B)
	{
		return (A * B >= 0);
	}

	static boolean OWN_PIECE(final int A, final int B)
	{
		return (A * B >  0);
	}

	public static boolean EMPTY_MOVE(final int A, final int B)
	{
		return (A * B == 0);
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

	int genAll_xPawn(final int[] list, final int[] offset, final int From, final int type)
	{
		int next = 0;

		switch (type) {
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (int i = 0; offset[i] != 0; i++) {
				for (int to = From + offset[i]; ON_BOARD(to); to += offset[i]) {
					if (square[to] == Piece.EMPTY) {
						list[next++] = to;
						continue;
					} else if (CAPTURE_MOVE(square[From], square[to])) {
						list[next++] = to;
					}
					break;
				}
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; offset[i] != 0; i++) {
				final int to = From + offset[i];
				if (ON_BOARD(to) && ANY_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			break;
		}
		return next;
	}

	int genCapture_xPawn(final int[] list, final int[] offset, final int From, final int type)
	{
		int next = 0;

		switch (type) {
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; offset[i] != 0; i++) {
				final int to = From + offset[i];
				if (ON_BOARD(to) && CAPTURE_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (int i = 0; offset[i] != 0; i++) {
				for (int to = From + offset[i]; ON_BOARD(to); to += offset[i]) {
					if (square[to] == Piece.EMPTY)
						continue;
					else if (CAPTURE_MOVE(square[From], square[to]))
						list[next++] = to;
					break;
				}
			}
			break;
		}
		return next;
	}

	int genMove_xPawn(final int[] list, final int[] offset, final int From, final int type)
	{
		int next = 0;

		switch (type) {
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; offset[i] != 0; i++) {
				final int to = From + offset[i];
				if (ON_BOARD(to) && square[to] == Piece.EMPTY)
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (int i = 0; offset[i] != 0; i++) {
				for (int to = From + offset[i]; ON_BOARD(to); to += offset[i]) {
					if (square[to] == Piece.EMPTY)
						list[next++] = to;
					else
						break;
				}
			}
			break;
		}
		return next;
	}

	boolean fromto_xPawn(final int From, final int To, final int type, final int[] offset)
	{
		final int diff = Math.abs(From - To);
		int n = 2;

		switch (type) {
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; i < 4; i++) {
				if (diff == offset[i])
					return ANY_MOVE(square[From], square[To]);
			}
			break;
		case Piece.QUEEN:
			n = 4;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int i = 0; i < n; i++) {
				if (diff % offset[i] == 0) {
					if (OWN_PIECE(square[From], square[To]))
						return false;
					i += ((To - From > 0)? 0 : n);
					for (int k = From + offset[i]; ON_BOARD(k); k += offset[i]) {
						if (k == To)
							return true;
						else if (square[k] != Piece.EMPTY)
							return false;
					}
				}
			}
			break;
		}
		return false;
	}

	boolean isAttacked_xBishop(final int From, final int FromColor)
	{
		// ROOK
		int[] offset = offsets[Piece.ROOK];
		for (int i = 0; offset[i] != 0; i++) {
			for (int to = From + offset[i], k = 1; ON_BOARD(to); to += offset[i], k++) {
				if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(FromColor, square[to]))
					break;
				else if (k == 1 && Math.abs(square[to]) == Piece.KING)
					return true;
				else if (Math.abs(square[to]) == Piece.ROOK || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else
					break;
			}
		}
		// KNIGHT
		offset = offsets[Piece.KNIGHT];
		for (int i = 0; offset[i] != 0; i++) {
			final int to = From + offset[i];
			if (OFF_BOARD(to))
				continue;
			else if (OWN_PIECE(FromColor, square[to]))
				continue;
			else if (Math.abs(square[to]) == Piece.KNIGHT)
				return true;
		}
		return false;
	}

	boolean attackLine(final int From, final int To)
	{
		if (OFF_BOARD(From | To))
			return false;

		final int diff = Math.abs(From - To);

		if (DistDB.TABLE[diff].step == 0)
			return false;

		final DistDB db = DistDB.TABLE[diff];
		switch (db.type) {
		case Piece.KNIGHT:
			return (Math.abs(square[To]) == Piece.KNIGHT && CAPTURE_MOVE(square[From], square[To]));
		case Piece.BISHOP:
			return attackLine_Bishop(db, From, To);
		case Piece.ROOK:
			final int offset = db.step * ((To > From)? 1:-1);
			for (int to = From + offset, k = 1; ON_BOARD(to); to += offset, k++) {
				if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(square[From], square[to]))
					break;
				else if (k == 1 && Math.abs(square[to]) == Piece.KING)
					return true;
				else if (Math.abs(square[to]) == Piece.ROOK || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else
					break;
			}
			break;
		}
		return false;
	}

	int parseZfen_Board(final String pos)
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
					return -1;
				loc++;
			} else if (st[n] == ':') {
				n++;
				break;
			} else {
				return -1;
			}
		}
		return n;
	}

	void printZfen_Board(final StringBuilder fen)
	{
		for (int i = 0, empty = 0; i < 64; i++) {
			// convert cordinate system
			final int n = SFF88(i);
			if (square[n] == Piece.EMPTY) {
				empty++;
				continue;
			} else if (empty != 0) {
				fen.append(empty);
			}
			if (square[n] > Piece.EMPTY)
				fen.append(Move.pieceSymbol[square[n]]);
			else
				fen.append(String.valueOf(Move.pieceSymbol[-square[n]]).toLowerCase(Locale.US));
			empty = 0;
		}
		fen.append(':');
	}
}
