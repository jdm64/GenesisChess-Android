package com.chess.genesis;

class Move
{
	public static final char[] pieceSymbol = {' ', 'P', 'N', 'B', 'R', 'Q', 'K'};

	public int index;
	public int xindex;
	public int from;
	public int to;

	public Move()
	{
		index = xindex = from = to = -1;
	}

	private StringBuffer printLoc(int loc)
	{
		StringBuffer str = new StringBuffer();

		if (loc > Piece.PLACEABLE) {
			str.append((char)((int)'a' + (loc % 8)));
			str.append((char)((int)'8' - (loc / 8)));
			return str;
		} else if (loc == Piece.PLACEABLE) {
			str = new StringBuffer("aval");
		} else {
			str = new StringBuffer("dead");
		}
		return str;
	}

	@Override
	public String toString()
	{
		StringBuffer out = new StringBuffer();

		if (from == Piece.PLACEABLE)
			out.append(pieceSymbol[Math.abs(Board.pieceType[index])]);
		else
			out = printLoc(from);
		out.append(printLoc(to));
		return out.toString();
	}

	public boolean parse(String str)
	{
		char[] s = str.toCharArray();
		int piece = Piece.NONE;
		boolean place = true;

		switch (s[0]) {
		case 'a':	case 'b':
		case 'c':	case 'd':
		case 'e':	case 'f':
		case 'g':	case 'h':
			place = false;
			break;
		case 'P':
			piece = Piece.PAWN;
			break;
		case 'N':
			piece = Piece.KNIGHT;
			break;
		case 'B':
			piece = Piece.BISHOP;
			break;
		case 'R':
			piece = Piece.ROOK;
			break;
		case 'Q':
			piece = Piece.QUEEN;
			break;
		case 'K':
			piece = Piece.KING;
			break;
		default:
			return false;
		}
		if (place) {
			// parse placement move
			if (s[1] < 'a' || s[1] > 'h' || s[2] < '0' || s[2] > '9')
				return false;
			to = s[1] - 'a';
			to += 8 * (8 - (s[2] - '0'));
			from = Piece.PLACEABLE;
			index = piece;
		} else {
			// parse movement move
			if (s[0] < 'a' || s[0] > 'h' || s[1] < '0' || s[1] > '9' ||
					s[2] < 'a' || s[2] > 'h' || s[3] < '0' || s[3] > '9')
				return false;
			from = s[0] - 'a';
			from += 8 * (8 - (s[1] - '0'));
			to = s[2] - 'a';
			to += 8 * (8 - (s[3] - '0'));
		}
		return true;
	}
}
