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
import android.os.*;

public abstract class Move implements Parcelable, Supplier<Move>
{
	public final static int NUM_MASK = 0x07;
	public final static int EP_FLAG = 1 << 3;
	public final static int CASTLE_KS = 1 << 4;
	public final static int CASTLE_QS = 1 << 5;
	public final static int CASTLE_FLAG = (CASTLE_KS | CASTLE_QS);
	public final static int PROMOTE_FLAG = 1 << 6;
	public final static int PLACE_FLAG = 1 << 7;

	public static final char[] PIECE_SYM = {'k', 'q', 'r', 'b', 'n', 'p', ' ', 'P', 'N', 'B', 'R', 'Q', 'K'};

	public int index;
	public int xindex;
	public int from;
	public int to;

	// only for RegMove
	public int flags;

	Move()
	{
		index = xindex = from = to = Piece.NULL_MOVE;
	}

	Move(final Parcel in)
	{
		index = in.readInt();
		xindex = in.readInt();
		from = in.readInt();
		to = in.readInt();
	}

	public static String printSq(int sq)
	{
		return "" + (char)('a' + (sq & 7)) + (char)('1' + (sq >> 4));
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int Flags)
	{
		out.writeInt(index);
		out.writeInt(xindex);
		out.writeInt(from);
		out.writeInt(to);
	}

	public void set(final Move move)
	{
		index = move.index;
		xindex = move.xindex;
		from = move.from;
		to = move.to;
	}

	public boolean isNull()
	{
		return index < 0;
	}

	public void setNull()
	{
		index = Piece.NULL_MOVE;
	}

	public int getCastle()
	{
		return 0;
	}

	public void setCastle(int side)
	{
	}

	public void setEnPassant()
	{
	}

	public boolean getEnPassant()
	{
		return false;
	}

	public void setPromote(int type)
	{
	}

	public int getPromote()
	{
		return 0;
	}

	public boolean isPromote(int stm)
	{
		return false;
	}

	protected abstract StringBuilder printLoc(final int loc);

	public abstract boolean parse(final String str);
}
