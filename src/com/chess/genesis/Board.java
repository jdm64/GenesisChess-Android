package com.chess.genesis;

public class Board
{
	public static final int NOT_MATE = 1;
	public static final int CHECK_MATE = 2;
	public static final int STALE_MATE = 3;
	
	public static final int VALID_MOVE = 0;
	public static final int INVALID_FORMAT = 1;
	public static final int NOPIECE_ERROR = 2;
	public static final int DONT_OWN = 3;
	public static final int KING_FIRST = 4;
	public static final int NON_EMPTY_PLACE = 5;
	public static final int CAPTURE_OWN = 6;
	public static final int INVALID_MOVEMENT = 7;
	public static final int IN_CHECK = 8;
	public static final int IN_CHECK_PLACE = 9;

	public static final int[] pieceType = {
		Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
		Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
		Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_BISHOP,
		Piece.BLACK_ROOK,   Piece.BLACK_ROOK,   Piece.BLACK_QUEEN,  Piece.BLACK_KING,
		Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
		Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
		Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_BISHOP,
		Piece.WHITE_ROOK,   Piece.WHITE_ROOK,   Piece.WHITE_QUEEN,  Piece.WHITE_KING};

	private int[] square;
	private int[] piece;

	private int stm;
	private int ply;
	
	public Board()
	{
		reset();
	}

	public void reset()
	{
		square = new int[64];
		for (int i = 0; i < 64; i++)
			square[i] = Piece.EMPTY;
		piece = new int[32];
		for (int i = 0; i < 32; i++)
			piece[i] = Piece.PLACEABLE;
		stm = Piece.WHITE;
		ply = 0;
	}

	public int getStm()
	{
		return stm;
	}

	private int pieceIndex(int loc)
	{
		for (int i = 0; i < 32; i++)
			if (piece[i] == loc)
				return i;
		return Piece.NONE;
	}

	private int pieceIndex(int loc, int type)
	{
		final int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};
		int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];

		for (int i = start; i < end; i++) {
			if (piece[i] == loc)
				return i;
		}
		return Piece.NONE;
	}

	public int kingIndex(int color)
	{
		return (Piece.WHITE == color)? piece[31] : piece[15];
	}

	public void make(Move move)
	{
		// update board information
		square[move.to] = pieceType[move.index];
		if (move.from != Piece.PLACEABLE)
				square[move.from] = Piece.EMPTY;
		// update piece information
		piece[move.index] = move.to;
		if (move.xindex != Piece.NONE)
			piece[move.xindex] = Piece.DEAD;

		stm ^= -2;
		ply++;
	}

	public void unmake(Move move)
	{
		// TODO could this function fail?
		piece[move.index] = move.from;
		if (move.xindex == Piece.NONE) {
			square[move.to] = Piece.EMPTY;
		} else {
			square[move.to] = pieceType[move.xindex];
			piece[move.xindex] = move.to;
		}
		if (move.from != Piece.PLACEABLE)
			square[move.from] = pieceType[move.index];

		stm ^= -2;
		ply--;

	}

	public boolean incheck(int color)
	{
		MoveLookup ml = new MoveLookup(square);
		int king = (color == Piece.WHITE)? 31:15;

		return (piece[king] != Piece.PLACEABLE)? ml.isAttacked(piece[king]) : false;
	}

	public int isMate()
	{
		if (getNumMoves(stm) != 0)
			return NOT_MATE;
		if (incheck(stm))
			return CHECK_MATE;
		else
			return STALE_MATE;
	}

	public int validMove(Move move)
	{
		// setup move.(x)index
		if (move.from == Piece.PLACEABLE) {
			move.index = pieceIndex(Piece.PLACEABLE, move.index * stm);
			if (move.index == Piece.NONE)
				return NOPIECE_ERROR;
			move.xindex = pieceIndex(move.to);
			if (move.xindex != Piece.NONE)
				return NON_EMPTY_PLACE;
		} else {
			move.index = pieceIndex(move.from);
			if (move.index == Piece.NONE)
				return NOPIECE_ERROR;
			else if (square[move.from] * stm < 0)
				return DONT_OWN;
			move.xindex = pieceIndex(move.to);
			if (move.xindex != Piece.NONE && square[move.to] * stm > 0)
				return CAPTURE_OWN;
		}
		// must place king first
		if (ply < 2 && Math.abs(pieceType[move.index]) != Piece.KING)
			return KING_FIRST;

		if (move.from != Piece.PLACEABLE) {
			MoveLookup ml = new MoveLookup(square);
			if (!ml.fromto(move.from, move.to))
				return INVALID_MOVEMENT;
		}
		int ret = VALID_MOVE;

		make(move);
		// curr is opponent after make
		if (incheck(stm ^ -2))
			ret = IN_CHECK;
		if (move.from == Piece.PLACEABLE && incheck(stm))
			ret = IN_CHECK_PLACE;
		unmake(move);

		return ret;
	}

	public int getNumMoves(int color)
	{
		MoveLookup movelookup = new MoveLookup(square);
		int num = 0;
		Move move = new Move();

		// we must place king first
		if (ply < 2) {
			int idx = pieceIndex(Piece.PLACEABLE, Piece.KING * color);

			for (int loc = 0; loc < 64; loc++) {
				if (square[loc] != Piece.EMPTY)
					continue;
				move.to = loc;
				move.index = idx;
				move.xindex = Piece.NONE;
				move.from = Piece.PLACEABLE;

				make(move);
				// place moves are only valid if neither side is inCheck
				if (!incheck(color) && !incheck(color ^ -2))
					num++;
				unmake(move);
			}
			return num;
		}
		// generate piece moves
		int start = (color == Piece.BLACK)? 0:16, end = (color == Piece.BLACK)? 16:32;
		for (int idx = start; idx < end; idx++) {
			if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
				continue;
			int n = 0;
			int[] loc = movelookup.genAll(piece[idx]);
			while (loc[n] != -1) {
				move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
				move.to = loc[n];
				move.from = piece[idx];
				move.index = idx;

				make(move);
				if (!incheck(color))
					num++;
				unmake(move);

				n++;
			}
		}
		// generate piece place moves
		for (int type = Piece.PAWN; type <= Piece.KING; type++) {
			int idx = pieceIndex(Piece.PLACEABLE, type * color);
			if (idx == Piece.NONE)
				continue;
			for (int loc = 0; loc < 64; loc++) {
				if (square[loc] != Piece.EMPTY)
					continue;
				move.index = idx;
				move.to = loc;
				move.xindex = Piece.NONE;
				move.from = Piece.PLACEABLE;

				make(move);
				// place moves are only valid if neither side is inCheck
				if (!incheck(color) && !incheck(color ^ -2))
				num++;
				unmake(move);
			}
		}
		return num;
	}
}
