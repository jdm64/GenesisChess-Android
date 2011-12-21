package com.chess.genesis;

import android.os.Parcel;

abstract class Move
{
	public static final char[] pieceSymbol = {' ', 'P', 'N', 'B', 'R', 'Q', 'K'};

	public int index;
	public int xindex;
	public int from;
	public int to;

	public Move()
	{
		index = xindex = from = to = -1;
	}

	public Move(final Move move)
	{
		index = move.index;
		xindex = move.xindex;
		from = move.from;
		to = move.to;
	}

	public Move(final Parcel in)
	{
		index = in.readInt();
		xindex = in.readInt();
		from = in.readInt();
		to = in.readInt();
	}

	public int describeContents()
	{
		return 0;
	}

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
		if (index == Piece.NULL_MOVE && xindex == Piece.NULL_MOVE && from == Piece.NULL_MOVE && to == Piece.NULL_MOVE)
			return true;
		return false;
	}

	public Move setNull()
	{
		index = xindex = from = to = Piece.NULL_MOVE;
		return this;
	}

	protected abstract StringBuffer printLoc(final int loc);

	public abstract boolean parse(final String str);
}
