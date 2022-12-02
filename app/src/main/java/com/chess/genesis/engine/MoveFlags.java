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

public class MoveFlags implements Supplier<MoveFlags>
{
	public final static int EP_FILE = 0x7;
	public final static int EP_FLAG = 1 << 3;
	public final static int EP_MASK = EP_FLAG | EP_FILE;
	public final static int WK_CASTLE = 1 << 4;
	public final static int WQ_CASTLE = 1 << 5;
	public final static int BK_CASTLE = 1 << 6;
	public final static int BQ_CASTLE = 1 << 7;
	public final static int W_CASTLE = WK_CASTLE | WQ_CASTLE;
	public final static int B_CASTLE = BK_CASTLE | BQ_CASTLE;
	public final static int WB_CASTLE = W_CASTLE | B_CASTLE;
	public final static int DEFAULT_FLAGS = W_CASTLE | B_CASTLE;

	public int bits;

	public MoveFlags()
	{
		reset();
	}

	public MoveFlags(final MoveFlags flags)
	{
		bits = flags.bits;
	}

	@Override
	public MoveFlags get()
	{
		return new MoveFlags();
	}

	public void set(final MoveFlags flags)
	{
		bits = flags.bits;
	}

	public final void reset()
	{
		bits = DEFAULT_FLAGS;
	}

	public boolean canEnPassant()
	{
		return (bits & EP_FLAG) != 0;
	}

	public int enPassantFile()
	{
		return bits & EP_FILE;
	}

	public void setEnPassant(final int file)
	{
		bits = (bits & ~EP_MASK) | EP_FLAG | file;
	}

	public void clearEnPassant()
	{
		bits &= ~EP_MASK;
	}

	public boolean canCastle(final int color)
	{
		return (bits & (color == Piece.WHITE ? W_CASTLE : B_CASTLE)) != 0;
	}

	public boolean canKingCastle(final int color)
	{
		return (bits & (color == Piece.WHITE ? WK_CASTLE : BK_CASTLE)) != 0;
	}

	public boolean canQueenCastle(final int color)
	{
		return (bits & (color == Piece.WHITE ? WQ_CASTLE : BQ_CASTLE)) != 0;
	}

	public void clearCastle(final int color)
	{
		bits &= color == Piece.WHITE ? ~W_CASTLE : ~B_CASTLE;
	}

	public void clearKingCastle(final int color)
	{
		bits &= color == Piece.WHITE ? ~WK_CASTLE : ~BK_CASTLE;
	}

	public void clearQueenCastle(final int color)
	{
		bits &= color == Piece.WHITE ? ~WQ_CASTLE : ~BQ_CASTLE;
	}

	public void setCastle(final int value)
	{
		bits = (bits & ~WB_CASTLE) | value;
	}
}
