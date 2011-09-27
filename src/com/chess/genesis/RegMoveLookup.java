package com.chess.genesis;

class RegMoveLookup extends MoveLookup
{
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
					} else if (CAPTURE_MOVE(square[From], square[to]) && to == To) {
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
					} else if (CAPTURE_MOVE(square[From], square[to]) && to == To) {
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
