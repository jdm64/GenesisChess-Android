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

public abstract class Move implements Parcelable
{
	public static final int[] InitPieceType = {
		Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
		Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
		Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_BISHOP,
		Piece.BLACK_ROOK,   Piece.BLACK_ROOK,   Piece.BLACK_QUEEN,  Piece.BLACK_KING,
		Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
		Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
		Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_BISHOP,
		Piece.WHITE_ROOK,   Piece.WHITE_ROOK,   Piece.WHITE_QUEEN,  Piece.WHITE_KING};

	public static final int VALID_MOVE = 0;
	public static final int INVALID_FORMAT = 1;
	public static final int NOPIECE_ERROR = 2;
	public static final int DONT_OWN = 3;
	public static final int KING_FIRST = 4;
	public static final int NON_EMPTY_PLACE = 5;
	public static final int CAPTURE_OWN = 6;
	public static final int INVALID_MOVEMENT = 7;
	public static final int IN_CHECK = 8;
	public static final int IN_CHECK_PLACE = 9;
	public static final int CANT_CASTLE = 10;

	public static final int MOVE_ALL = 0;
	public static final int MOVE_CAPTURE = 1;
	public static final int MOVE_MOVE = 2;
	public static final int MOVE_PLACE = 3;

	public static final int NOT_MATE = 1;
	public static final int CHECK_MATE = 2;
	public static final int STALE_MATE = 3;

	public final static int EP_FILE = 0x07;
	public final static int CAN_EP = 0x08;
	public final static int CASTLE_KS = 0x10;
	public final static int CASTLE_QS = 0x20;
	public final static int WK_CASTLE = 0x10;
	public final static int WQ_CASTLE = 0x20;
	public final static int BK_CASTLE = 0x40;
	public final static int BQ_CASTLE = 0x80;
	public final static int W_CASTLE = (WK_CASTLE | WQ_CASTLE);
	public final static int B_CASTLE = (BK_CASTLE | BQ_CASTLE);
	public final static int K_CASTLE = (WK_CASTLE | BK_CASTLE);
	public final static int Q_CASTLE = (WQ_CASTLE | BQ_CASTLE);

	public static final char[] pieceSymbol = {' ', 'P', 'N', 'B', 'R', 'Q', 'K'};

	public int index;
	public int xindex;
	public int from;
	public int to;

	// only for RegMove
	public int flags;

	public Move()
	{
		index = xindex = from = to = -1;
	}

	public Move(final Parcel in)
	{
		index = in.readInt();
		xindex = in.readInt();
		from = in.readInt();
		to = in.readInt();
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
		if (index == Piece.NULL_MOVE && xindex == Piece.NULL_MOVE && from == Piece.NULL_MOVE && to == Piece.NULL_MOVE)
			return true;
		return false;
	}

	public Move setNull()
	{
		index = xindex = from = to = Piece.NULL_MOVE;
		return this;
	}

	public abstract Move newInstance();
	public abstract int getCastle();
	public abstract void setCastle(final int side);
	public abstract void setEnPassant();
	public abstract boolean getEnPassant();
	public abstract void setPromote(final int type);
	public abstract int getPromote();

	protected abstract StringBuilder printLoc(final int loc);

	public abstract boolean parse(final String str);
}
