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

public final class Piece
{
	private Piece()
	{
	}

	public static final int WHITE = 1;
	public static final int BLACK = -1;

	public static final int NONE = -1;
	public static final int PLACEABLE = -2;
	public static final int DEAD = -4;
	public static final int NULL_MOVE = -8;

	public static final int PAWN = 1;
	public static final int KNIGHT = 2;
	public static final int BISHOP = 3;
	public static final int ROOK = 4;
	public static final int QUEEN = 5;
	public static final int KING = 6;

	public static final int BLACK_KING = -6;
	public static final int BLACK_QUEEN = -5;
	public static final int BLACK_ROOK = -4;
	public static final int BLACK_BISHOP = -3;
	public static final int BLACK_KNIGHT = -2;
	public static final int BLACK_PAWN = -1;
	public static final int EMPTY = 0;
	public static final int WHITE_PAWN = 1;
	public static final int WHITE_KNIGHT = 2;
	public static final int WHITE_BISHOP = 3;
	public static final int WHITE_ROOK = 4;
	public static final int WHITE_QUEEN = 5;
	public static final int WHITE_KING = 6;

	public static final int A1 = 0;
	public static final int B1 = 1;
	public static final int C1 = 2;
	public static final int D1 = 3;
	public static final int E1 = 4;
	public static final int F1 = 5;
	public static final int G1 = 6;
	public static final int H1 = 7;
	public static final int A2 = 16;
	public static final int B2 = 17;
	public static final int C2 = 18;
	public static final int D2 = 19;
	public static final int E2 = 20;
	public static final int F2 = 21;
	public static final int G2 = 22;
	public static final int H2 = 23;
	public static final int A3 = 32;
	public static final int B3 = 33;
	public static final int C3 = 34;
	public static final int D3 = 35;
	public static final int E3 = 36;
	public static final int F3 = 37;
	public static final int G3 = 38;
	public static final int H3 = 39;
	public static final int A4 = 48;
	public static final int B4 = 49;
	public static final int C4 = 50;
	public static final int D4 = 51;
	public static final int E4 = 52;
	public static final int F4 = 53;
	public static final int G4 = 54;
	public static final int H4 = 55;
	public static final int A5 = 64;
	public static final int B5 = 65;
	public static final int C5 = 66;
	public static final int D5 = 67;
	public static final int E5 = 68;
	public static final int F5 = 69;
	public static final int G5 = 70;
	public static final int H5 = 71;
	public static final int A6 = 80;
	public static final int B6 = 81;
	public static final int C6 = 82;
	public static final int D6 = 83;
	public static final int E6 = 84;
	public static final int F6 = 85;
	public static final int G6 = 86;
	public static final int H6 = 87;
	public static final int A7 = 96;
	public static final int B7 = 97;
	public static final int C7 = 98;
	public static final int D7 = 99;
	public static final int E7 = 100;
	public static final int F7 = 101;
	public static final int G7 = 102;
	public static final int H7 = 103;
	public static final int A8 = 112;
	public static final int B8 = 113;
	public static final int C8 = 114;
	public static final int D8 = 115;
	public static final int E8 = 116;
	public static final int F8 = 117;
	public static final int G8 = 118;
	public static final int H8 = 119;
}
