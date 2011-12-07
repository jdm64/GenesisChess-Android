package com.chess.genesis;

class GenPosition extends GenMoveLookup
{
	private static final int[] TYPE = {
		Piece.EMPTY,		Piece.EMPTY,		Piece.BLACK_KING,	Piece.WHITE_BISHOP,
		Piece.EMPTY,		Piece.BLACK_KNIGHT,	Piece.EMPTY,		Piece.BLACK_PAWN,
		Piece.BLACK_QUEEN,	Piece.BLACK_ROOK,	Piece.EMPTY,		Piece.EMPTY,
		Piece.WHITE_KING,	Piece.EMPTY,		Piece.BLACK_BISHOP,	Piece.WHITE_KNIGHT,
		Piece.EMPTY,		Piece.WHITE_PAWN,	Piece.WHITE_QUEEN,	Piece.WHITE_ROOK};

	public int[] piece;
	public int stm;
	public int ply;

	public GenPosition()
	{
		square = new int[64];
		piece = new int[32];
	}

	public GenPosition(final int[] _square, final int[] _piece, final int _ply)
	{
		square = IntArray.clone(_square);
		piece = IntArray.clone(_piece);
		ply = _ply;
	}

	private void parseReset()
	{
		for (int i = 0; i < 64; i++)
			square[i] = Piece.EMPTY;
		for (int i = 0; i < 32; i++)
			piece[i] = Piece.DEAD;
	}

	private boolean setPiece(final int loc, final int type)
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
		parseReset();
		final char[] st = pos.toCharArray();

		// index counter for pos
		int n = 0;

		// parse board
		StringBuffer num = new StringBuffer();
		boolean act = false;
		for (int loc = 0; true; n++) {
			if (Character.isDigit(st[n])) {
				num.append(st[n]);
				act = true;
			} else if (Character.isLetter(st[n])) {
				if (act) {
					loc += Integer.parseInt(num.toString());
					num = new StringBuffer();
					act = false;
				}
				if (!setPiece(loc, TYPE[st[n] % 21]))
					return false;
				loc++;
			} else if (st[n] == ':') {
				n++;
				break;
			} else {
				return false;
			}
		}

		// parse placeable pieces
		for (;; n++) {
			if (st[n] == ':') {
				n++;
				break;
			} else if (!Character.isLetter(st[n])) {
				return false;
			} else if (!setPiece(Piece.PLACEABLE, TYPE[st[n] % 21])) {
				return false;
			}
		}

		// parse half-ply
		num = new StringBuffer();
		while (Character.isDigit(st[n])) {
			num.append(st[n]);
			n++;
		}
		ply = Integer.valueOf(num.toString());

		int mply = 0;
		for (int i = 0; i < 32; i++) {
			switch (piece[i]) {
			case Piece.DEAD:
				mply += 2;
				break;
			case Piece.PLACEABLE:
				break;
			default:
				mply += 1;
				break;
			}
		}
		if (ply < mply)
			return false;

		// check if color not on move is in check
		stm = (ply % 2 == 1)? Piece.BLACK : Piece.WHITE;
		if (incheck(stm ^ -2))
			return false;
		return true;
	}

	public String printZfen()
	{
		final StringBuffer fen = new StringBuffer();

		for (int i = 0, empty = 0; i < 64; i++) {
			if (square[i] == Piece.EMPTY) {
				empty++;
				continue;
			}
			if (empty != 0)
				fen.append(empty);
			if (square[i] > Piece.EMPTY)
				fen.append(Move.pieceSymbol[square[i]]);
			else
				fen.append(String.valueOf(Move.pieceSymbol[-square[i]]).toLowerCase());
			empty = 0;
		}
		fen.append(':');

		for (int i = 0; i < 16; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(String.valueOf(Move.pieceSymbol[-GenBoard.pieceType[i]]).toLowerCase());
		}
		for (int i = 16; i < 32; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(Move.pieceSymbol[GenBoard.pieceType[i]]);
		}
		fen.append(':');
		fen.append(ply);

		return fen.toString();
	}

	public boolean equal(final GenPosition pos)
	{
		final String a = pos.printZfen(), b = printZfen();
		
		return a.equals(b);
	}
}
