package com.chess.genesis;

import android.os.Parcel;

abstract class Move
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
