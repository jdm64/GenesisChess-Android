package com.chess.genesis;

class RegMoveLookup
{
	private static final int[] mailbox = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1,  0,  1,  2,  3,  4,  5,  6,  7, -1,
		-1,  8,  9, 10, 11, 12, 13, 14, 15, -1,
		-1, 16, 17, 18, 19, 20, 21, 22, 23, -1,
		-1, 24, 25, 26, 27, 28, 29, 30, 31, -1,
		-1, 32, 33, 34, 35, 36, 37, 38, 39, -1,
		-1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
		-1, 48, 49, 50, 51, 52, 53, 54, 55, -1,
		-1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

	private static final int[] mailbox64 = {
		21, 22, 23, 24, 25, 26, 27, 28,
		31, 32, 33, 34, 35, 36, 37, 38,
		41, 42, 43, 44, 45, 46, 47, 48,
		51, 52, 53, 54, 55, 56, 57, 58,
		61, 62, 63, 64, 65, 66, 67, 68,
		71, 72, 73, 74, 75, 76, 77, 78,
		81, 82, 83, 84, 85, 86, 87, 88,
		91, 92, 93, 94, 95, 96, 97, 98};

	private static final int[][] offsets = {
		{  0,   0,   0,  0,   0,  0,  0,  0},
		{-11,  -9,   9, 11, -10, -1,  1, 10},
		{-21, -19, -12, -8,   8, 12, 19, 21},
		{-11,  -9,   9, 11,   0,  0,  0,  0},
		{-10,  -1,   1, 10,   0,  0,  0,  0},
		{-11, -10,  -9, -1,   1,  9, 10, 11},
		{-11, -10,  -9, -1,   1,  9, 10, 11} };

	public int[] square;

	private int COL(final int x)
	{
		return x & 7;
	}

	private boolean NOT_CAPTURE(final int A, final int B)
	{
		return (A * B >= 0);
	}
	
	private boolean OWN_PIECE(final int A, final int B)
	{
		return (A * B >  0);
	}

	private boolean EMPTY_MOVE(final int A, final int B)
	{
		return (A * B == 0);
	}
	
	private boolean CAPTURE_MOVE(final int A, final int B)
	{
		return (A * B <  0);
	}

	private boolean ANY_MOVE(final int A, final int B)
	{
		return (A * B <= 0);
	}

	public int[] genAll(final int From)
	{
		final int type = Math.abs(square[From]), mfrom = mailbox64[From];
		final int[] offset = offsets[type], list = new int[28];
		int next = 0;

		switch (type) {
		case Piece.PAWN:
			if (square[From] > 0) { // WHITE
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 9]))
					list[next++] = From - 9;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 7]))
					list[next++] = From - 7;
				if (square[From - 8] == 0) {
					list[next++] = From - 8;
					if (From >= 48 && square[From - 16] == 0)
						list[next++] = From - 16;
				}
			} else { // BLACK
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 7]))
					list[next++] = From + 7;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 9]))
					list[next++] = From + 9;
				if (square[From + 8] == 0) {
					list[next++] = From + 8;
					if (From <= 15 && square[From + 16] == 0)
						list[next++] = From + 16;
				}
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				final int to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (ANY_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (square[to] == Piece.EMPTY) {
						list[next++] = to;
						continue;
					} else if (CAPTURE_MOVE(square[From], square[to])) {
						list[next++] = to;
					}
					break;
				}
			}
			break;
		case Piece.QUEEN:
			for (int dir = 0; dir < 8; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (square[to] == Piece.EMPTY) {
						list[next++] = to;
						continue;
					} else if (CAPTURE_MOVE(square[From], square[to])) {
						list[next++] = to;
					}
					break;
				}
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	public int[] genCapture(final int From)
	{
		final int type = Math.abs(square[From]),  mfrom = mailbox64[From];
		final int[] offset = offsets[type], list = new int[28];
		int next = 0;

		switch (type) {
		case Piece.PAWN:
			if (square[From] > 0) { // WHITE
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 9]))
					list[next++] = From - 9;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 7]))
					list[next++] = From - 7;
			} else { // BLACK
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 7]))
					list[next++] = From + 7;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 9]))
					list[next++] = From + 9;
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				final int to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (CAPTURE_MOVE(square[From], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1)
						break;
					else if (square[to] == Piece.EMPTY)
						continue;
					else if (CAPTURE_MOVE(square[From], square[to]))
						list[next++] = to;
					break;
				}
			}
			break;
		case Piece.QUEEN:
			for (int dir = 0; dir < 8; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1)
						break;
					else if (square[to] == Piece.EMPTY)
						continue;
					else if (CAPTURE_MOVE(square[From], square[to]))
						list[next++] = to;
					break;
				}
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	public int[] genMove(final int From)
	{
		final int type = Math.abs(square[From]), mfrom = mailbox64[From];
		final int[] offset = offsets[type], list = new int[28];
		int next = 0;

		switch (type) {
		case Piece.PAWN:
			if (square[From] > 0) { // WHITE
				if (square[From - 8] == 0) {
					list[next++] = From - 8;
					if (From >= 48 && square[From - 16] == 0)
						list[next++] = From - 16;
				}
			} else { // BLACK
				if (square[From + 8] == 0) {
					list[next++] = From + 8;
					if (From <= 15 && square[From + 16] == 0)
						list[next++] = From + 16;
				}
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				final int to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (square[to] == Piece.EMPTY)
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (square[to] == Piece.EMPTY) {
						list[next++] = to;
						continue;
					}
					break;
				}
			}
			break;
		case Piece.QUEEN:
			for (int dir = 0; dir < 8; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (square[to] == Piece.EMPTY) {
						list[next++] = to;
						continue;
					}
					break;
				}
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	public boolean fromto(final int From, final int To)
	{
		final int type = Math.abs(square[From]), mfrom = mailbox64[From];
		final int[] offset = offsets[type];

		switch (type) {
		case Piece.PAWN:
			if (square[From] > 0) { // WHITE
				if (From - 9 == To && COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 9]))
					return true;
				if (From - 7 == To && COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 7]))
					return true;
				if (square[From - 8] == 0) {
					if (From - 8 == To)
						return true;
					if (From >= 48 && square[From - 16] == 0 && From - 16 == To)
						return true;
				}
			} else { // BLACK
				if (From + 7 == To && COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 7]))
					return true;
				if (From + 9 == To && COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 9]))
					return true;
				if (square[From + 8] == 0) {
					if (From + 8 == To)
						return true;
					if (From <= 15 && square[From + 16] == 0 && From + 16 == To)
						return true;
				}
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				final int to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (ANY_MOVE(square[From], square[to]) && to == To)
					return true;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (square[to] == Piece.EMPTY) {
						if (to == To)
							return true;
						continue;
					} else if (CAPTURE_MOVE(square[From], square[to])) {
						if (to == To)
							return true;
					}
					break;
				}
			}
			break;
		case Piece.QUEEN:
			for (int dir = 0; dir < 8; dir++) {
				for (int k = 1; k < 8; k++) {
					final int to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (square[to] == Piece.EMPTY) {
						if (to == To)
							return true;
						continue;
					} else if (CAPTURE_MOVE(square[From], square[to])) {
						if (to == To)
							return true;
					}
					break;
				}
			}
			break;
		}
		return false;
	}

	public boolean isAttacked(final int From, final int Bycolor)
	{
		final int mfrom = mailbox64[From];

		for (int dir = 0; dir < 4; dir++) {
			for (int k = 1; k < 8; k++) {
				final int to = mailbox[mfrom + k * offsets[Piece.ROOK][dir]];
				if (to == -1)
					break;
				else if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(square[to], Bycolor))
					break;
				else if (Math.abs(square[to]) == Piece.ROOK || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else if (k == 1 && Math.abs(square[to]) == Piece.KING)
					return true;
				break;
			}
		}

		for (int dir = 0; dir < 4; dir++) {
			for (int k = 1; k < 8; k++) {
				final int to = mailbox[mfrom + k * offsets[Piece.BISHOP][dir]];
				if (to == -1) {
					break;
				} else if (square[to] == Piece.EMPTY) {
					continue;
				} else if (OWN_PIECE(square[to], Bycolor)) {
					break;
				} else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN) {
					return true;
				} else if (k == 1) {
					if (Math.abs(square[to]) == Piece.PAWN && Bycolor * (From - to) > 0)
						return true;
					else if (Math.abs(square[to]) == Piece.KING)
						return true;
				}
				break;
			}
		}

		for (int dir = 0; dir < 8; dir++) {
			final int to = mailbox[mfrom + offsets[Piece.KNIGHT][dir]];
			if (to == -1)
				continue;
			else if (NOT_CAPTURE(square[to], Bycolor))
				continue;
			else if (Math.abs(square[to]) == Piece.KNIGHT)
				return true;
		}
		return false;
	}
}
