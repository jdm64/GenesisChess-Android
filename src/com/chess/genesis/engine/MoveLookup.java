package com.chess.genesis;

class MoveLookup
{
	public static final int[] stype = {
		Piece.EMPTY,		Piece.EMPTY,		Piece.BLACK_KING,	Piece.WHITE_BISHOP,
		Piece.EMPTY,		Piece.BLACK_KNIGHT,	Piece.EMPTY,		Piece.BLACK_PAWN,
		Piece.BLACK_QUEEN,	Piece.BLACK_ROOK,	Piece.EMPTY,		Piece.EMPTY,
		Piece.WHITE_KING,	Piece.EMPTY,		Piece.BLACK_BISHOP,	Piece.WHITE_KNIGHT,
		Piece.EMPTY,		Piece.WHITE_PAWN,	Piece.WHITE_QUEEN,	Piece.WHITE_ROOK};

	protected static final int[][] offsets = {
		{ 0,  0,   0,   0,   0,   0,   0,   0, 0, 0},
		{17, 16,  15,   1, -17, -16, -15,  -1, 0, 0}, // Pawn: even=capture
		{33, 31,  18,  14, -33, -31, -18, -14, 0, 0}, // Knight
		{17, 15, -17, -15,   0,   0,   0,   0, 0, 0}, // Bishop
		{16,  1, -16,  -1,   0,   0,   0,   0, 0, 0}, // Rook
		{17, 16,  15,   1, -17, -16, -15,  -1, 0, 0}, // Queen
		{17, 16,  15,   1, -17, -16, -15,  -1, 0, 0} }; // King

	public int[] square;
	public int[] piece;
	public int[] piecetype;
	public int ply;
	public int stm;

	public static int COL(final int x)
	{
		return x & 7;
	}

	public static boolean NOT_CAPTURE(final int A, final int B)
	{
		return (A * B >= 0);
	}

	public static boolean OWN_PIECE(final int A, final int B)
	{
		return (A * B >  0);
	}

	public static boolean EMPTY_MOVE(final int A, final int B)
	{
		return (A * B == 0);
	}

	public static boolean CAPTURE_MOVE(final int A, final int B)
	{
		return (A * B <  0);
	}

	public static boolean ANY_MOVE(final int A, final int B)
	{
		return (A * B <= 0);
	}

	public static int EE64(final int x)
	{
		return (x >> 1) | (x & 7);
	}

	public static int EE64F(final int x)
	{
		return ((7 - (x >> 4)) << 3) + (x & 7);
	}

	public static int SF88(final int x)
	{
		return (x & ~7) + x;
	}

	public static int SFF88(final int x)
	{
		return ((7 - (x >> 3)) << 4) + (x & 7);
	}

	public int genAll_xPawn(final int[] list, final int[] offset, final int From, final int type)
	{
		int next = 0;

		switch (type) {
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (int i = 0; offset[i] != 0; i++) {
				for (int to = From + offset[i]; (to & 0x88) == 0; to += offset[i]) {
					if (square[to] == Piece.EMPTY) {
						list[next++] = to;
						continue;
					} else if (CAPTURE_MOVE(square[From], square[to])) {
						list[next++] = to;
					}
					break;
				}
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; offset[i] != 0; i++) {
				final int to = From + offset[i];
				if ((to & 0x88) != 0)
					continue;
				else if (ANY_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			break;
		}
		return next;
	}

	public int genCapture_xPawn(final int[] list, final int[] offset, final int From, final int type)
	{
		int next = 0;

		switch (type) {
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; offset[i] != 0; i++) {
				final int to = From + offset[i];
				if ((to & 0x88) != 0)
					continue;
				else if (CAPTURE_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (int i = 0; offset[i] != 0; i++) {
				for (int to = From + offset[i]; (to & 0x88) == 0; to += offset[i]) {
					if (square[to] == Piece.EMPTY)
						continue;
					else if (CAPTURE_MOVE(square[From], square[to]))
						list[next++] = to;
					break;
				}
			}
			break;
		}
		return next;
	}

	public int genMove_xPawn(final int[] list, final int[] offset, final int From, final int type)
	{
		int next = 0;

		switch (type) {
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; offset[i] != 0; i++) {
				final int to = From + offset[i];
				if ((to & 0x88) != 0)
					continue;
				else if (square[to] == Piece.EMPTY)
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
		case Piece.QUEEN:
			for (int i = 0; offset[i] != 0; i++) {
				for (int to = From + offset[i]; (to & 0x88) == 0; to += offset[i]) {
					if (square[to] == Piece.EMPTY)
						list[next++] = to;
					else
						break;
				}
			}
			break;
		}
		return next;
	}

	public boolean fromto_xPawn(final int From, final int To, final int type, final int[] offset)
	{
		final int diff = Math.abs(From - To);
		int n = 2;

		switch (type) {
		case Piece.KNIGHT:
		case Piece.KING:
			for (int i = 0; i < 4; i++) {
				if (diff == offset[i])
					return ANY_MOVE(square[From], square[To]);
			}
			break;
		case Piece.QUEEN:
			n = 4;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int i = 0; i < n; i++) {
				if (diff % offset[i] == 0) {
					if (OWN_PIECE(square[From], square[To]))
						return false;
					i += ((To - From > 0)? 0 : n);
					for (int k = From + offset[i]; (k & 0x88) == 0; k += offset[i]) {
						if (k == To)
							return true;
						else if (square[k] != Piece.EMPTY)
							return false;
					}
				}
			}
			break;
		}
		return false;
	}

	public boolean isAttacked_xBishop(final int From, final int FromColor)
	{
		// ROOK
		int[] offset = offsets[Piece.ROOK];
		for (int i = 0; offset[i] != 0; i++) {
			for (int to = From + offset[i], k = 1; (to & 0x88) == 0; to += offset[i], k++) {
				if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(FromColor, square[to]))
					break;
				else if (k == 1 && Math.abs(square[to]) == Piece.KING)
					return true;
				else if (Math.abs(square[to]) == Piece.ROOK || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else
					break;
			}
		}
		// KNIGHT
		offset = offsets[Piece.KNIGHT];
		for (int i = 0; offset[i] != 0; i++) {
			final int to = From + offset[i];
			if ((to & 0x88) != 0)
				continue;
			else if (OWN_PIECE(FromColor, square[to]))
				continue;
			else if (Math.abs(square[to]) == Piece.KNIGHT)
				return true;
		}
		return false;
	}
}
