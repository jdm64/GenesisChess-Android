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

public interface Piece
{
	int WHITE = 1;
	int BLACK = -1;

	int NONE = -1;
	int PLACEABLE = -2;
	int DEAD = -4;
	int NULL_MOVE = -8;

	int PAWN = 1;
	int KNIGHT = 2;
	int BISHOP = 3;
	int ROOK = 4;
	int QUEEN = 5;
	int KING = 6;

	int BLACK_KING = -6;
	int BLACK_QUEEN = -5;
	int BLACK_ROOK = -4;
	int BLACK_BISHOP = -3;
	int BLACK_KNIGHT = -2;
	int BLACK_PAWN = -1;
	int EMPTY = 0;
	int WHITE_PAWN = 1;
	int WHITE_KNIGHT = 2;
	int WHITE_BISHOP = 3;
	int WHITE_ROOK = 4;
	int WHITE_QUEEN = 5;
	int WHITE_KING = 6;

	int A1 = 0;
	int B1 = 1;
	int C1 = 2;
	int D1 = 3;
	int E1 = 4;
	int F1 = 5;
	int G1 = 6;
	int H1 = 7;
	int A2 = 16;
	int B2 = 17;
	int C2 = 18;
	int D2 = 19;
	int E2 = 20;
	int F2 = 21;
	int G2 = 22;
	int H2 = 23;
	int A3 = 32;
	int B3 = 33;
	int C3 = 34;
	int D3 = 35;
	int E3 = 36;
	int F3 = 37;
	int G3 = 38;
	int H3 = 39;
	int A4 = 48;
	int B4 = 49;
	int C4 = 50;
	int D4 = 51;
	int E4 = 52;
	int F4 = 53;
	int G4 = 54;
	int H4 = 55;
	int A5 = 64;
	int B5 = 65;
	int C5 = 66;
	int D5 = 67;
	int E5 = 68;
	int F5 = 69;
	int G5 = 70;
	int H5 = 71;
	int A6 = 80;
	int B6 = 81;
	int C6 = 82;
	int D6 = 83;
	int E6 = 84;
	int F6 = 85;
	int G6 = 86;
	int H6 = 87;
	int A7 = 96;
	int B7 = 97;
	int C7 = 98;
	int D7 = 99;
	int E7 = 100;
	int F7 = 101;
	int G7 = 102;
	int H7 = 103;
	int A8 = 112;
	int B8 = 113;
	int C8 = 114;
	int D8 = 115;
	int E8 = 116;
	int F8 = 117;
	int G8 = 118;
	int H8 = 119;
}
