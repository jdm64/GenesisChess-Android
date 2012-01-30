package com.chess.genesis;

class RegMoveLookup extends MoveLookup
{
	public int[] genAll(final int From)
	{
		final int type = Math.abs(square[From]);
		final int[] list = new int[28];
		int next = 0;

		if (type == Piece.PAWN) {
			if (square[From] == Piece.WHITE_PAWN) { // WHITE
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 15]))
					list[next++] = From + 15;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 17]))
					list[next++] = From + 17;
				if (square[From + 16] == 0) {
					list[next++] = From + 16;
					if (From <= Piece.H2 && square[From + 32] == 0)
						list[next++] = From + 32;
				}
			} else { // BLACK
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 17]))
					list[next++] = From - 17;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 15]))
					list[next++] = From - 15;
				if (square[From - 16] == 0) {
					list[next++] = From - 16;
					if (From >= Piece.A7 && square[From - 32] == 0)
						list[next++] = From - 32;
				}
			}
		} else {
			next = genAll_xPawn(list, offsets[type], From, type);
		}
		list[next] = -1;
		return list;
	}

	public int[] genCapture(final int From)
	{
		final int type = Math.abs(square[From]); //  mfrom = mailbox64[From];
		int[] list = new int[28];
		int next = 0;

		if (type == Piece.PAWN) {
			if (square[From] == Piece.WHITE_PAWN) { // WHITE
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 15]))
					list[next++] = From + 15;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 17]))
					list[next++] = From + 17;
			} else { // BLACK
				if (COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 17]))
					list[next++] = From - 17;
				if (COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 15]))
					list[next++] = From - 15;
			}
		} else {
			next = genCapture_xPawn(list, offsets[type], From, type);
		}
		list[next] = -1;
		return list;
	}

	public int[] genMove(final int From)
	{
		final int type = Math.abs(square[From]);
		int[] list = new int[28];
		int next = 0;

		if (type == Piece.PAWN) {
			if (square[From] == Piece.WHITE_PAWN) { // WHITE
				if (square[From + 16] == 0) {
					list[next++] = From + 16;
					if (From <= Piece.H2 && square[From + 32] == 0)
						list[next++] = From + 32;
				}
			} else { // BLACK
				if (square[From - 16] == 0) {
					list[next++] = From - 16;
					if (From >= Piece.A7 && square[From - 32] == 0)
						list[next++] = From - 32;
				}
			}
		} else {
			next = genMove_xPawn(list, offsets[type], From, type);
		}
		list[next] = -1;
		return list;
	}

	public boolean fromto(final int From, final int To)
	{
		final int type = Math.abs(square[From]);

		if (type == Piece.PAWN) {
			if (square[From] == Piece.WHITE_PAWN) { // WHITE
				if (From + 15 == To && COL(From) != 0 && CAPTURE_MOVE(square[From], square[From + 15]))
					return true;
				if (From + 17 == To && COL(From) != 7 && CAPTURE_MOVE(square[From], square[From + 17]))
					return true;
				if (square[From + 16] == 0) {
					if (From + 16 == To)
						return true;
					if (From + 32 == To && From <= Piece.H2 && square[From + 32] == 0)
						return true;
				}
			} else { // BLACK
				if (From - 17 == To && COL(From) != 0 && CAPTURE_MOVE(square[From], square[From - 17]))
					return true;
				if (From - 15 == To && COL(From) != 7 && CAPTURE_MOVE(square[From], square[From - 15]))
					return true;
				if (square[From - 16] == 0) {
					if (From - 16 == To)
						return true;
					else if (From - 32 == To && From >= Piece.A7 && square[From - 32] == 0)
						return true;
				}
			}
		} else {
			return fromto_xPawn(From, To, type, offsets[type]);
		}
		return false;
	}

	public boolean attackLine_Bishop(final DistDB db, final int From, final int To)
	{
		final int offset = db.step * ((To > From)? 1:-1);
		for (int to = From + offset, k = 1; (to & 0x88) == 0; to += offset, k++) {
			if (square[to] == Piece.EMPTY) {
				continue;
			} else if (OWN_PIECE(square[From], square[to])) {
				return false;
			} else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN) {
				return true;
			} else if (k == 1) {
				if (Math.abs(square[to]) == Piece.PAWN && square[From] * (to - From) > 0)
					return true;
				else if (Math.abs(square[to]) == Piece.KING)
					return true;
			}
			break;
		}
		return false;
	}

	public boolean attackLine(final int From, final int To)
	{
		if ((From & 0x88) != 0)
			return false;

		final int diff = Math.abs(From - To);

		if (DistDB.TABLE[diff].step == 0)
			return false;

		final DistDB db = DistDB.TABLE[diff];
		switch (db.type) {
		case Piece.KNIGHT:
			return (Math.abs(square[To]) == Piece.KNIGHT && CAPTURE_MOVE(square[From], square[To]));
		case Piece.BISHOP:
			return attackLine_Bishop(db, From, To);
		case Piece.ROOK:
			final int offset = db.step * ((To > From)? 1:-1);
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

	public boolean isAttacked(final int From, final int FromColor)
	{
		final int[] offset = offsets[Piece.BISHOP];
		for (int i = 0; offset[i] != 0; i++) {
			for (int to = From + offset[i], k = 1; (to & 0x88) == 0; to += offset[i], k++) {
				if (square[to] == Piece.EMPTY) {
					continue;
				} else if (OWN_PIECE(FromColor, square[to])) {
					break;
				} else if (Math.abs(square[to]) == Piece.BISHOP || Math.abs(square[to]) == Piece.QUEEN) {
					return true;
				} else if (k == 1) {
					if (Math.abs(square[to]) == Piece.PAWN && FromColor * (to - From) > 0)
						return true;
					else if (Math.abs(square[to]) == Piece.KING)
						return true;
				}
				break;
			}
		}
		return isAttacked_xBishop(From, FromColor);
	}

}
