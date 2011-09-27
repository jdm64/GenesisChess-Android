package com.chess.genesis;

class GenMoveLookup extends MoveLookup
{
	public int[] genAll(final int from)
	{
		final int type = Math.abs(square[from]), mfrom = mailbox64[from];
		final int[] offset = offsets[type], list = new int[28];
		int next = 0, to;

		switch (type) {
		case Piece.PAWN:
			// captures
			for (int dir = 0; dir < 4; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (CAPTURE_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			// moves
			for (int dir = 4; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (EMPTY_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (ANY_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (EMPTY_MOVE(square[from], square[to])) {
						list[next++] = to;
						continue;
					} else if (CAPTURE_MOVE(square[from], square[to])) {
						list[next++] = to;
					}
					break;
				}
			}
			break;
		case Piece.QUEEN:
			for (int dir = 0; dir < 8; dir++) {
				for (int k = 1; k < 8; k++) {
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (EMPTY_MOVE(square[from], square[to])) {
						list[next++] = to;
						continue;
					} else if (CAPTURE_MOVE(square[from], square[to])) {
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

	public int[] genCapture(final int from)
	{
		final int type = Math.abs(square[from]), mfrom = mailbox64[from];
		final int[] offset = offsets[type], list = new int[28];
		int next = 0, to;

		switch (type) {
		case Piece.PAWN:
			// captures
			for (int dir = 0; dir < 4; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (CAPTURE_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (CAPTURE_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1)
						break;
					else if (EMPTY_MOVE(square[from], square[to]))
						continue;
					else if (CAPTURE_MOVE(square[from], square[to]))
						list[next++] = to;
					break;
				}
			}
			break;
		case Piece.QUEEN:
			for (int dir = 0; dir < 8; dir++) {
				for (int k = 1; k < 8; k++) {
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1)
						break;
					else if (EMPTY_MOVE(square[from], square[to]))
						continue;
					else if (CAPTURE_MOVE(square[from], square[to]))
						list[next++] = to;
					break;
				}
			}
			break;
		}
		list[next] = -1;
		return list;
	}

	public int[] genMove(final int from)
	{
		final int type = Math.abs(square[from]), mfrom = mailbox64[from];
		final int[] offset = offsets[type], list = new int[28];
		int next = 0, to;

		switch (type) {
		case Piece.PAWN:
			// moves
			for (int dir = 4; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (EMPTY_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (EMPTY_MOVE(square[from], square[to]))
					list[next++] = to;
			}
			break;
		case Piece.BISHOP:
		case Piece.ROOK:
			for (int dir = 0; dir < 4; dir++) {
				for (int k = 1; k < 8; k++) {
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (EMPTY_MOVE(square[from], square[to])) {
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
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (EMPTY_MOVE(square[from], square[to])) {
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
		int to;

		switch (type) {
		case Piece.PAWN:
			// captures
			for (int dir = 0; dir < 4; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (CAPTURE_MOVE(square[From], square[to]) && to == To)
					return true;
			}
			// moves
			for (int dir = 4; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
				if (to == -1)
					continue;
				else if (EMPTY_MOVE(square[From], square[to]) && to == To)
					return true;
			}
			break;
		case Piece.KNIGHT:
		case Piece.KING:
			for (int dir = 0; dir < 8; dir++) {
				to = mailbox[mfrom + offset[dir]];
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
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (EMPTY_MOVE(square[From], square[to])) {
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
					to = mailbox[mfrom + k * offset[dir]];
					if (to == -1) {
						break;
					} else if (EMPTY_MOVE(square[From], square[to])) {
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

	public boolean isAttacked(final int from)
	{
		final int mfrom = mailbox64[from];
		int to;

		int[] offset = offsets[Piece.ROOK];
		for (int dir = 0; dir < 4; dir++) {
			for (int k = 1; k < 8; k++) {
				to = mailbox[mfrom + k * offset[dir]];
				if (to == -1)
					break;
				else if (EMPTY_MOVE(square[from], square[to]))
					continue;
				else if (OWN_PIECE(square[from], square[to]))
					break;
				else if (Math.abs(square[to]) == Piece.ROOK || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else if (k == 1 && Math.abs(square[to]) == Piece.KING)
					return true;
				break;
			}
		}

		offset = offsets[Piece.BISHOP];
		for (int dir = 0; dir < 4; dir++) {
			for (int k = 1; k < 8; k++) {
				to = mailbox[mfrom + k * offset[dir]];
				if (to == -1)
					break;
				else if (EMPTY_MOVE(square[from], square[to]))
					continue;
				else if (OWN_PIECE(square[from], square[to]))
					break;
				else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else if (k == 1 && (Math.abs(square[to]) == Piece.PAWN || Math.abs(square[to]) == Piece.KING))
					return true;
				break;
			}
		}

		offset = offsets[Piece.KNIGHT];
		for (int dir = 0; dir < 8; dir++) {
			to = mailbox[mfrom + offset[dir]];
			if (to == -1)
				continue;
			else if (NOT_CAPTURE(square[from], square[to]))
				continue;
			else if (Math.abs(square[to]) == Piece.KNIGHT)
				return true;
		}
		return false;
	}
}
