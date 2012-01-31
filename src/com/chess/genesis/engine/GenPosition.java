package com.chess.genesis;

class GenPosition extends GenMoveLookup
{
	public GenPosition()
	{
		square = new int[128];
		piece = new int[32];
		piecetype = new int[32];
	}

	protected void parseReset()
	{
		for (int i = 0; i < 128; i++)
			square[i] = Piece.EMPTY;
		for (int i = 0; i < 32; i++) {
			piece[i] = Piece.DEAD;
			piecetype[i] = Move.InitPieceType[i];
		}
	}

	private void setMaxPly()
	{
		int tply = 0;
		for (int i = 0; i < 32; i++) {
			if (piece[i] == Piece.DEAD)
				tply += 2;
			else if (piece[i] != Piece.PLACEABLE)
				tply++;
		}
		ply = Math.max(ply, tply);

		if (stm == Piece.WHITE) {
			if (ply % 2 == 1)
				ply++;
		} else if (ply % 2 == 0) {
			ply++;
		}
	}

	protected boolean setPiece(final int loc, final int type)
	{
		final int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};
		final int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];

		for (int i = start; i < end; i++) {
			if (piece[i] == Piece.DEAD) {
				piece[i] = loc;
				if (loc != Piece.PLACEABLE)
					square[loc] = type;
				return true;
			}
		}
		return false;
	}

	public boolean incheck(final int color)
	{
		final int king = (color == Piece.WHITE)? 31:15;

		return (piece[king] == Piece.PLACEABLE)? false : isAttacked(piece[king]);
	}

	public boolean parseZfen(final String pos)
	{
		int n = parseZfen_Board(pos);

		// check if board parsing failed
		if (n <= 0)
			return false;

		// parse placeable pieces
		final char[] st = pos.toCharArray();
		for (;; n++) {
			if (st[n] == ':') {
				n++;
				break;
			} else if (!Character.isLetter(st[n])) {
				return false;
			} else if (!setPiece(Piece.PLACEABLE, stype[st[n] % 21])) {
				return false;
			}
		}

		// parse half-ply
		final StringBuffer num = new StringBuffer();
		while (Character.isDigit(st[n])) {
			num.append(st[n]);
			n++;
		}
		ply = Integer.valueOf(num.toString());
		stm = (ply % 2 == 1)? Piece.BLACK : Piece.WHITE;

		setMaxPly();

		// check if color not on move is in check
		if (incheck(stm ^ -2))
			return false;
		return true;
	}

	public String printZfen()
	{
		final StringBuffer fen = new StringBuffer();

		printZfen_Board(fen);

		for (int i = 0; i < 16; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(String.valueOf(Move.pieceSymbol[-Move.InitPieceType[i]]).toLowerCase());
		}
		for (int i = 16; i < 32; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(Move.pieceSymbol[Move.InitPieceType[i]]);
		}
		fen.append(':');
		fen.append(ply);

		return fen.toString();
	}
}
