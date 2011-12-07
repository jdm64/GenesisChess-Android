package com.chess.genesis;

import android.os.Parcel;
import android.os.Parcelable;

class RegMove extends Move implements Parcelable
{
	public int flags;

	public static final Parcelable.Creator<RegMove> CREATOR = new Parcelable.Creator<RegMove>()
	{
		public RegMove createFromParcel(final Parcel in)
		{
			return new RegMove(in);
		}

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

	public RegMove setNull()
	{
		super.setNull();
		flags = 0;
		return this;
	}

	public int getCastle()
	{
		return flags & 0x30;
	}

	public void setCastle(final int side)
	{
		flags = side & 0x30;
	}

	public void setEnPassant()
	{
		flags = 0x8;
	}

	public boolean getEnPassant()
	{
		return (flags & 0x8) != 0;
	}

	public void setPromote(final int type)
	{
		flags = 0x7 & type;
	}

	public int getPromote()
	{
		return flags & 0x7;
	}

	public int type()
	{
		if (xindex != Piece.NONE)
			return RegBoard.MOVE_CAPTURE;
		else
			return RegBoard.MOVE_MOVE;
	}

	protected StringBuffer printLoc(final int loc)
	{
		final StringBuffer str = new StringBuffer();

		if (loc > Piece.PLACEABLE) {
			str.append((char)((int)'a' + (loc % 8)));
			str.append((char)((int)'8' - (loc / 8)));
			return str;
		} else {
			str.append("dead");
		}
		return str;
	}

	@Override
	public String toString()
	{
		if (getCastle() != 0) {
			if (getCastle() == 0x10)
				return "O-O";
			else
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
				setCastle(0x10);
				return true;
			}
			if (s[3] != '-')
				return false;
			if (s[4] != 'O' && s[4] != 'o' && s[4] != '0')
				return false;
			setCastle(0x20);
			return true;
		}

		if (s[0] < 'a' || s[0] > 'h' || s[1] < '1' || s[1] > '8' ||
				s[2] < 'a' || s[2] > 'h' || s[3] < '1' || s[3] > '8')
			return false;

		from = s[0] - 'a';
		from += 8 * (8 - (s[1] - '0'));
		to = s[2] - 'a';
		to += 8 * (8 - (s[3] - '0'));

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
