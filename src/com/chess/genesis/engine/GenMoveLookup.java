package com.chess.genesis;

class GenMoveLookup extends MoveLookup
{
	public int[] genAll(final int From)
	{
		final int type = Math.abs(square[From]);
		int[] offset = offsets[type];
		int[] list = new int[28];
		int next = 0;

		if (type == Piece.PAWN) {
			boolean evn = true;
			for (int i = 0; offset[i] != 0; i++, evn ^= true) {
				final int to = From + offset[i];
				if ((to & 0x88) != 0)
					continue;
				final boolean val = evn? CAPTURE_MOVE(square[From], square[to]) : (square[to] == Piece.EMPTY);
				if (val)
					list[next++] = to;
			}
		} else {
			next = genAll_xPawn(list, offset, From, type);
		}
		list[next] = -1;
		return list;
	}

	public int[] genCapture(final int From)
	{
		final int type = Math.abs(square[From]);
		int[] offset = offsets[type];
		int[] list = new int[28];
		int next = 0;

		if (type == Piece.PAWN) {
			// captures
			for (int i = 0; offset[i] != 0; i += 2) {
				final int to = From + offset[i];
				if ((to & 0x88) != 0)
					continue;
				else if (CAPTURE_MOVE(square[From], square[to]))
					list[next++] = to;
			}
		} else {
			next = genCapture_xPawn(list, offset, From, type);
		}
		list[next] = -1;
		return list;
	}

	public int[] genMove(final int From)
	{
		final int type = Math.abs(square[From]);
		int[] offset = offsets[type];
		int[] list = new int[28];
		int next = 0;

		if (type == Piece.PAWN) {
			// moves
			for (int i = 1; offset[i] != 0; i += 2) {
				final int to = From + offset[i];
				if ((to & 0x88) != 0)
					continue;
				else if (square[to] == Piece.EMPTY)
					list[next++] = to;
			}
		} else {
			next = genMove_xPawn(list, offset, From, type);
		}
		list[next] = -1;
		return list;
	}

	public boolean fromto(final int From, final int To)
	{
		if (((From | To) & 0x88) != 0)
			return false;

		final int type = Math.abs(square[From]);
		int[] offset = offsets[type];

		if (type == Piece.PAWN) {
			final int diff = Math.abs(From - To);
			for (int i = 0; i < 4; i++) {
				if (diff == offset[i])
					return ((i%2 != 0)? (square[To] == Piece.EMPTY) : CAPTURE_MOVE(square[From], square[To]));
			}
		} else {
			return fromto_xPawn(From, To, type, offset);
		}
		return false;
	}

	public boolean attackLine_Bishop(final DistDB db, final int From, final int To)
	{
		int offset = db.step * ((To > From)? 1:-1);
		for (int to = From + offset, k = 1; (to & 0x88) == 0; to += offset, k++) {
			if (square[to] == Piece.EMPTY)
				continue;
			else if (OWN_PIECE(square[From], square[to]))
				return false;
			else if (k == 1 && (Math.abs(square[to]) == Piece.PAWN || Math.abs(square[to]) == Piece.KING))
				return true;
			else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN)
				return true;
			break;
		}
		return false;
	}

	public boolean attackLine(final int From, final int To)
	{
		if (((From | To) & 0x88) != 0)
			return false;

		int diff = Math.abs(From - To);

		if (DistDB.TABLE[diff].step == 0)
			return false;

		DistDB db = DistDB.TABLE[diff];
		switch (db.type) {
		case Piece.KNIGHT:
			return (Math.abs(square[To]) == Piece.KNIGHT && CAPTURE_MOVE(square[From], square[To]));
		case Piece.BISHOP:
			return attackLine_Bishop(db, From, To);
		case Piece.ROOK:
			int offset = db.step * ((To > From)? 1:-1);
			for (int to = From + offset, k = 1; (to & 0x88) == 0; to += offset, k++) {
				if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(square[From], square[to]))
					break;
				else if (k == 1 && Math.abs(square[to]) == Piece.KING)
					return true;
				else if (Math.abs(square[to]) == Piece.ROOK || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else
					break;
			}
			break;
		}
		return false;
	}

	public boolean isAttacked(final int From)
	{
		// BISHOP
		int[] offset = offsets[Piece.BISHOP];
		for (int i = 0; offset[i] != 0; i++) {
			for (int to = From + offset[i], k = 1; (to & 0x88) == 0; to += offset[i], k++) {
				if (square[to] == Piece.EMPTY)
					continue;
				else if (OWN_PIECE(square[From], square[to]))
					break;
				else if (k == 1 && (Math.abs(square[to]) == Piece.PAWN || Math.abs(square[to]) == Piece.KING))
					return true;
				else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN)
					return true;
				else
					break;
			}
		}
		return isAttacked_xBishop(From, square[From]);
	}
}
