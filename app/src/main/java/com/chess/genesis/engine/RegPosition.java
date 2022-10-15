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

abstract class RegPosition extends RegMoveLookup
{
	// for setPiece()
	private final static int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};

	public static final int[] InitRegPiece = {
		Piece.A7, Piece.B7, Piece.C7, Piece.D7, Piece.E7, Piece.F7, Piece.G7, Piece.H7,
		Piece.B8, Piece.G8, Piece.C8, Piece.F8, Piece.A8, Piece.H8, Piece.D8, Piece.E8,
		Piece.A2, Piece.B2, Piece.C2, Piece.D2, Piece.E2, Piece.F2, Piece.G2, Piece.H2,
		Piece.B1, Piece.G1, Piece.C1, Piece.F1, Piece.A1, Piece.H1, Piece.D1, Piece.E1};

	final MoveFlags flags;

	RegPosition()
	{
		square = new int[128];
		piece = new int[32];
		pieceType = new int[32];
		flags = new MoveFlags();
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
		flags.reset();
	}

	@Override
	public void setMaxPly()
	{
		int tPly = 0;
		for (int i = 0; i < 32; i++) {
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
		final int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];

		// try pieces in their initial location
		for (int i = start; i < end; i++) {
			if (InitRegPiece[i] == loc && piece[i] == Piece.DEAD) {
				piece[i] = loc;
				square[loc] = type;
				return true;
			}
		}
		// piece moved but not promoted
		for (int i = start; i < end; i++) {
			if (piece[i] == Piece.DEAD) {
				piece[i] = loc;
				square[loc] = type;
				return true;
			}
		}

		// piece might be a promote
		if (Math.abs(type) == Piece.PAWN || Math.abs(type) == Piece.KING)
			return false;

		final int pStart = (type > 0)? 16:0, pend = (type > 0)? 24:8;
		for (int i = pStart; i < pend; i++) {
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
		final int king = (color == Piece.WHITE)? 31:15;
		return isAttacked(piece[king], color);
	}

	@Override
	public int parseZFen_Specific(int n, String pos)
	{
		final char[] st = pos.toCharArray();

		// parse castle rights
		int castle = 0;
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
			int eps = (st[n++] - 'a');
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
}
