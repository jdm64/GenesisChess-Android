/*	GenChess, a genesis chess engine
	Copyright (C) 2012, Justin Madru (justin.jdm64@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.chess.genesis.engine;

import android.os.*;

public class RegMove extends Move implements Parcelable
{
	public int flags;

	public static final Parcelable.Creator<RegMove> CREATOR = new Parcelable.Creator<RegMove>()
	{
		@Override
		public RegMove createFromParcel(final Parcel in)
		{
			return new RegMove(in);
		}

		@Override
		public RegMove[] newArray(final int size)
		{
			return new RegMove[size];
		}
	};

	public RegMove()
	{
		super();
		flags = 0;
	}

	public RegMove(final RegMove move)
	{
		super(move);
		flags = move.flags;
	}

	public RegMove(final Parcel in)
	{
		super(in);
		flags = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel out, final int Flags)
	{
		super.writeToParcel(out, Flags);
		out.writeInt(flags);
	}

	public void set(final RegMove move)
	{
		super.set(move);
		flags = move.flags;
	}

	@Override
	public RegMove setNull()
	{
		super.setNull();
		flags = 0;
		return this;
	}

	public int getCastle()
	{
		return flags & (CASTLE_KS | CASTLE_QS);
	}

	public void setCastle(final int side)
	{
		flags = side & (CASTLE_KS | CASTLE_QS);
	}

	public void setEnPassant()
	{
		flags = CAN_EP;
	}

	public boolean getEnPassant()
	{
		return (flags & CAN_EP) != 0;
	}

	public void setPromote(final int type)
	{
		flags = type & 0x07;
	}

	public int getPromote()
	{
		return flags & 0x07;
	}

	public int type()
	{
		if (xindex != Piece.NONE)
			return Move.MOVE_CAPTURE;
		return Move.MOVE_MOVE;
	}

	@Override
	protected StringBuffer printLoc(final int loc)
	{
		final StringBuffer str = new StringBuffer();

		if (loc > Piece.PLACEABLE) {
			str.append((char) ('a' + (loc & 7)));
			str.append((char) ('1' + (loc >> 4)));
		} else {
			str.append("dead");
		}
		return str;
	}

	@Override
	public String toString()
	{
		if (getCastle() != 0) {
			if (getCastle() == CASTLE_KS)
				return "O-O";
			return "O-O-O";
		}
		final StringBuffer out = new StringBuffer();

		out.append(printLoc(from));
		out.append(printLoc(to));

		switch (getPromote()) {
		case 2:
			out.append('N');
			break;
		case 3:
			out.append('B');
			break;
		case 4:
			out.append('R');
			break;
		case 5:
			out.append('Q');
			break;
		}
		return out.toString();
	}

	@Override
	public boolean parse(final String str)
	{
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
		}

		if (s[0] < 'a' || s[0] > 'h' || s[1] < '1' || s[1] > '8' ||
				s[2] < 'a' || s[2] > 'h' || s[3] < '1' || s[3] > '8')
			return false;

		from = 16 * (s[1] - '1') + (s[0] - 'a');
		to = 16 * (s[3] - '1') + (s[2] - 'a');

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
		return true;
	}
}
