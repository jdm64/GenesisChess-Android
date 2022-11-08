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
		bits = Move.DEFAULT_FLAGS;
	}

	public int canEnPassant()
	{
		return bits & Move.CAN_EP;
	}

	public int enPassantFile()
	{
		return bits & Move.EP_FILE;
	}

	public void setEnPassant(final int file)
	{
		bits = (bits & ~Move.EP_FLAG) | (file | Move.CAN_EP);
	}

	public void clearEnPassant()
	{
		bits &= ~Move.EP_FLAG;
	}

	public int canCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? Move.W_CASTLE : Move.B_CASTLE);
	}

	public int canKingCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? Move.WK_CASTLE : Move.BK_CASTLE);
	}

	public int canQueenCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? Move.WQ_CASTLE : Move.BQ_CASTLE);
	}

	public void clearCastle(final int color)
	{
		bits &= ((color == Piece.WHITE)? ~Move.W_CASTLE : ~Move.B_CASTLE);
	}

	public void clearKingCastle(final int color)
	{
		bits &= ((color == Piece.WHITE)? ~Move.WK_CASTLE : ~Move.BK_CASTLE);
	}

	public void clearQueenCastle(final int color)
	{
		bits &= ((color == Piece.WHITE)? ~Move.WQ_CASTLE : ~Move.BQ_CASTLE);
	}

	public void setCastle(final int value)
	{
		bits &= (0xff & value);
	}
}
