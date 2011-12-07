package com.chess.genesis;

import android.os.Parcel;
import android.os.Parcelable;

class GenMove extends Move implements Parcelable
{
	public static final Parcelable.Creator<GenMove> CREATOR = new Parcelable.Creator<GenMove>()
	{
		public GenMove createFromParcel(final Parcel in)
		{
			return new GenMove(in);
		}

		public GenMove[] newArray(final int size)
		{
			return new GenMove[size];
		}
	};

	public GenMove()
	{
		super();
	}

	public GenMove(final GenMove move)
	{
		super(move);
	}

	public GenMove(final Parcel in)
	{
		super(in);
	}

	public GenMove setNull()
	{
		super.setNull();
		return this;
	}

	protected StringBuffer printLoc(final int loc)
	{
		final StringBuffer str = new StringBuffer();

		if (loc > Piece.PLACEABLE) {
			str.append((char)((int)'a' + (loc % 8)));
			str.append((char)((int)'8' - (loc / 8)));
			return str;
		} else if (loc == Piece.PLACEABLE) {
			str.append("aval");
		} else {
			str.append("dead");
		}
		return str;
	}

	@Override
	public String toString()
	{
		final StringBuffer out = new StringBuffer();

		if (from == Piece.PLACEABLE)
			out.append(pieceSymbol[Math.abs(GenBoard.pieceType[index])]);
		else
			out.append(printLoc(from));
		out.append(printLoc(to));
		return out.toString();
	}

	public boolean parse(final String str)
	{
		final char[] s = str.toCharArray();
		int piece = Piece.NONE;
		boolean place = true;

		switch (s[0]) {
		case 'a':	case 'b':
		case 'c':	case 'd':
		case 'e':	case 'f':
		case 'g':	case 'h':
			place = false;
			break;
		case 'P':
			piece = Piece.PAWN;
			break;
		case 'N':
			piece = Piece.KNIGHT;
			break;
		case 'B':
			piece = Piece.BISHOP;
			break;
		case 'R':
			piece = Piece.ROOK;
			break;
		case 'Q':
			piece = Piece.QUEEN;
			break;
		case 'K':
			piece = Piece.KING;
			break;
		default:
			return false;
		}
		if (place) {
			// parse placement move
			if (s[1] < 'a' || s[1] > 'h' || s[2] < '0' || s[2] > '9')
				return false;
			to = s[1] - 'a';
			to += 8 * (8 - (s[2] - '0'));
			from = Piece.PLACEABLE;
			index = piece;
		} else {
			// parse movement move
			if (s[0] < 'a' || s[0] > 'h' || s[1] < '0' || s[1] > '9' ||
					s[2] < 'a' || s[2] > 'h' || s[3] < '0' || s[3] > '9')
				return false;
			from = s[0] - 'a';
			from += 8 * (8 - (s[1] - '0'));
			to = s[2] - 'a';
			to += 8 * (8 - (s[3] - '0'));
		}
		return true;
	}
}
