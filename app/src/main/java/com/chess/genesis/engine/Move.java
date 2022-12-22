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

import android.os.*;
import androidx.annotation.*;

public class Move implements Parcelable
{
	public final static int NUM_MASK = 0x07;
	public final static int EP_FLAG = 1 << 3;
	public final static int CASTLE_KS = 1 << 4;
	public final static int CASTLE_QS = 1 << 5;
	public final static int CASTLE_FLAG = (CASTLE_KS | CASTLE_QS);
	public final static int PROMOTE_FLAG = 1 << 6;
	public final static int PLACE_FLAG = 1 << 7;

	public static final char[] PIECE_SYM = {'k', 'q', 'r', 'b', 'n', 'p', ' ', 'P', 'N', 'B', 'R', 'Q', 'K'};

	public static final Creator<Move> CREATOR = new Creator<>()
	{
		@Override
		public Move createFromParcel(Parcel in)
		{
			return new Move(in);
		}

		@Override
		public Move[] newArray(int size)
		{
			return new Move[size];
		}
	};

	public int index;
	public int xindex;
	public int from;
	public int to;
	public int flags;

	public Move()
	{
		index = xindex = from = to = Piece.NULL_MOVE;
		flags = 0;
	}

	public Move(Parcel in)
	{
		index = in.readInt();
		xindex = in.readInt();
		from = in.readInt();
		to = in.readInt();
		flags = in.readInt();
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
		out.writeInt(flags);
	}

	public void set(final Move move)
	{
		index = move.index;
		xindex = move.xindex;
		from = move.from;
		to = move.to;
		flags = move.flags;
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
		return flags & CASTLE_FLAG;
	}

	public void setCastle(int side)
	{
		flags = side & CASTLE_FLAG;
	}

	public void setEnPassant()
	{
		flags = EP_FLAG;
	}

	public boolean getEnPassant()
	{
		return (flags & EP_FLAG) != 0;
	}

	public int getPlace()
	{
		return (flags & PLACE_FLAG) != 0 ? flags & NUM_MASK : 0;
	}

	public void setPlace(int piece)
	{
		flags = PLACE_FLAG | (piece & NUM_MASK);
	}

	public static boolean couldBePromote(int to, int stm)
	{
		return stm == Piece.WHITE ? (to >= Piece.A8) : (to <= Piece.H1);
	}

	public void setPromote(int type)
	{
		flags = PROMOTE_FLAG | (type & NUM_MASK);
	}

	public int getPromote()
	{
		return (flags & PROMOTE_FLAG) != 0 ? (flags & NUM_MASK) : 0;
	}

	public boolean parse(String str)
	{
		if (str == null || str.isEmpty())
			return false;
		final char[] s = str.toCharArray();

		switch (s[0]) {
		case 'O':
		case 'o':
		case '0':
			if (s[1] != '-')
				return false;

			if (s[2] != 'O' && s[2] != 'o' && s[2] != '0')
				return false;
			if (s.length == 3) {
				setCastle(CASTLE_KS);
				return true;
			}
			if (s[3] != '-')
				return false;
			if (s[4] != 'O' && s[4] != 'o' && s[4] != '0')
				return false;
			setCastle(CASTLE_QS);
			return true;
		case 'a':	case 'b':
		case 'c':	case 'd':
		case 'e':	case 'f':
		case 'g':	case 'h':
			break;
		case 'P':
			setPlace(Piece.PAWN);
			break;
		case 'N':
			setPlace(Piece.KNIGHT);
			break;
		case 'B':
			setPlace(Piece.BISHOP);
			break;
		case 'R':
			setPlace(Piece.ROOK);
			break;
		case 'Q':
			setPlace(Piece.QUEEN);
			break;
		case 'K':
			setPlace(Piece.KING);
			break;
		default:
			return false;
		}

		if (getPlace() != 0) {
			// parse placement move
			from = Piece.PLACEABLE;
			to = cordToIdx(s[1], s[2]);
			return to >= 0;
		}

		from = cordToIdx(s[0], s[1]);
		to = cordToIdx(s[2], s[3]);

		if (s.length == 5) {
			switch (s[4]) {
			case 'Q':
				setPromote(Piece.QUEEN);
				break;
			case 'R':
				setPromote(Piece.ROOK);
				break;
			case 'B':
				setPromote(Piece.BISHOP);
				break;
			case 'N':
				setPromote(Piece.KNIGHT);
				break;
			default:
				return false;
			}
		}
		return from >= 0 && to >= 0;
	}

	public static int cordToIdx(char a, char b)
	{
		if (a < 'a' || a > 'h' || b < '1' || b > '8')
			return Piece.NULL_MOVE;
		return 16 * (b - '1') + (a - 'a');
	}

	@NonNull
	@Override
	public String toString()
	{
		var castle = getCastle();
		if (castle != 0) {
			return castle == CASTLE_KS ? "O-O" : "O-O-O";
		}

		var out = new StringBuilder();

		var place = getPlace();
		if (place != 0) {
			out.append(PIECE_SYM[place + 6]);
		} else {
			out.append(printSq(from));
		}

		out.append(printSq(to));

		switch (getPromote()) {
		case Piece.KNIGHT:
			out.append('N');
			break;
		case Piece.BISHOP:
			out.append('B');
			break;
		case Piece.ROOK:
			out.append('R');
			break;
		case Piece.QUEEN:
			out.append('Q');
			break;
		}

		return out.toString();
	}
}
