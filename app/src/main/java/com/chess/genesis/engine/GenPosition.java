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

abstract class GenPosition extends GenMoveLookup
{
	// for setPiece()
	private final static int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};

	GenPosition()
	{
		square = new int[128];
		piece = new int[32];
		pieceType = new int[32];
	}

	@Override
	protected void parseReset()
	{
		for (int i = 0; i < 128; i++)
			square[i] = Piece.EMPTY;
		for (int i = 0; i < 32; i++) {
			piece[i] = Piece.DEAD;
			pieceType[i] = Move.InitPieceType[i];
		}
	}

	private void setMaxPly()
	{
		int tPly = 0;
		for (int i = 0; i < 32; i++) {
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
	protected boolean setPiece(final int loc, final int type)
	{
		final int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];

		for (int i = start; i < end; i++) {
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
		final int king = (color == Piece.WHITE)? 31:15;

		return (piece[king] != Piece.PLACEABLE) && isAttacked(piece[king]);
	}

	public boolean parseZFen(final String pos)
	{
		int n = parseZfen_Board(pos);

		// check if board parsing failed
		if (n <= 0)
			return false;

		// parse placeable pieces
		final char[] st = pos.toCharArray();
		for (;; n++) {
			if (st[n] == ':') {
				n++;
				break;
			} else if (!Character.isLetter(st[n])) {
				return false;
			} else if (!setPiece(Piece.PLACEABLE, stype[st[n] % 21])) {
				return false;
			}
		}

		// parse half-ply
		final StringBuilder num = new StringBuilder();
		while (n < st.length && Character.isDigit(st[n])) {
			num.append(st[n]);
			n++;
		}
		ply = Integer.parseInt(num.toString());
		stm = (ply % 2 != 0)? Piece.BLACK : Piece.WHITE;

		setMaxPly();

		// check if color not on move is in check
		return !inCheck(stm ^ -2);
	}

	public String printZFen()
	{
		final StringBuilder fen = new StringBuilder();

		printZfen_Board(fen);

		for (int i = 0; i < 16; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(String.valueOf(Move.pieceSymbol[-Move.InitPieceType[i]]).toLowerCase(Locale.US));
		}
		for (int i = 16; i < 32; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(Move.pieceSymbol[Move.InitPieceType[i]]);
		}
		fen.append(':');
		fen.append(ply);

		return fen.toString();
	}
}
