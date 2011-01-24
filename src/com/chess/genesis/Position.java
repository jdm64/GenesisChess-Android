package com.chess.genesis;

public class Position
{
	private static final int[] type = {
		Piece.EMPTY,		Piece.EMPTY,		Piece.BLACK_KING,	Piece.WHITE_BISHOP,
		Piece.EMPTY,		Piece.BLACK_KNIGHT,	Piece.EMPTY,		Piece.BLACK_PAWN,
		Piece.BLACK_QUEEN,	Piece.BLACK_ROOK,	Piece.EMPTY,		Piece.EMPTY,
		Piece.WHITE_KING,	Piece.EMPTY,		Piece.BLACK_BISHOP,	Piece.WHITE_KNIGHT,
		Piece.EMPTY,		Piece.WHITE_PAWN,	Piece.WHITE_QUEEN,	Piece.WHITE_ROOK};

	public int[] square;
	public int[] piece;

	public int ply;

	public void reset()
	{
		square = new int[64];
		for (int i = 0; i < 64; i++)
			square[i] = Piece.EMPTY;
		piece = new int[32];
		for (int i = 0; i < 32; i++)
			piece[i] = Piece.DEAD;
	}

	private boolean setPiece(int loc, int type)
	{
		final int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};
		int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
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

	private boolean incheck(int color)
	{
		MoveLookup ml = new MoveLookup(square);
		int king = (color == Piece.WHITE)? 31:15;

		return (piece[king] != Piece.PLACEABLE)? ml.isAttacked(piece[king]) : false;
	}

	public boolean parseZfen(String pos)
	{
		reset();
		char[] st = pos.toCharArray();

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
				if (!setPiece(loc, type[st[n] % 21]))
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
			} else if (!setPiece(Piece.PLACEABLE, type[st[n] % 21])) {
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
		int ctm = (ply % 2 == 1)? Piece.BLACK : Piece.WHITE;
		if (incheck(ctm ^ -2))
			return false;
		return true;
	}

	public String printZfen()
	{
		// StringBuffer buf = new StringBuffer();
		StringBuffer fen = new StringBuffer();

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
				fen.append(String.valueOf(Move.pieceSymbol[-Board.pieceType[i]]).toLowerCase());
		}
		for (int i = 16; i < 32; i++) {
			if (piece[i] == Piece.PLACEABLE)
				fen.append(Move.pieceSymbol[Board.pieceType[i]]);
		}
		fen.append(':');
		fen.append(ply);

		return fen.toString();
	}

	public boolean equal(Position pos)
	{
		String a = pos.printZfen(), b = printZfen();
		
		return a.equals(b);
	}
}