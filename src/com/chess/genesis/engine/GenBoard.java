package com.chess.genesis;

class GenBoard extends GenPosition
{
	public static final int[] pieceType = {
		Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
		Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,   Piece.BLACK_PAWN,
		Piece.BLACK_KNIGHT, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_BISHOP,
		Piece.BLACK_ROOK,   Piece.BLACK_ROOK,   Piece.BLACK_QUEEN,  Piece.BLACK_KING,
		Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
		Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,   Piece.WHITE_PAWN,
		Piece.WHITE_KNIGHT, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_BISHOP,
		Piece.WHITE_ROOK,   Piece.WHITE_ROOK,   Piece.WHITE_QUEEN,  Piece.WHITE_KING};

	private static final int[] typeLookup = {
		0, 0, 0, 0, 0, 0,  0,  0,
		1, 1, 2, 2, 3, 3,  4,  5,
		6, 6, 6, 6, 6, 6,  6,  6,
		7, 7, 8, 8, 9, 9, 10, 11};

	private static final int[] pieceValue = {
		224, 224, 224, 224, 224, 224, 224, 224,
		336, 336, 560, 560, 896, 896, 1456, 0};

	private static final int[][] locValue = {
	{	0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0},
	{	-5, 0, 0, 0, 0, 0, 0, -5,
/* Pawn */	 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		 0, 5, 5, 5, 5, 5, 5,  0,
		-5, 0, 0, 0, 0, 0, 0, -5},
	{	-10, -5,  0,  0,  0,  0, -5, -10,
/* Knight */	 -5,  0, 10, 10, 10, 10,  0,  -5,
		  0, 10, 20, 20, 20, 20, 10,   0,
		  0, 10, 20, 20, 20, 20, 10,   0,
		  0, 10, 20, 20, 20, 20, 10,   0,
		  0, 10, 20, 20, 20, 20, 10,   0,
		 -5,  0, 10, 10, 10, 10,  0,  -5,
		-10, -5,  0,  0,  0,  0, -5, -10},
	{	-10, -10, -10, -10, -10, -10, -10, -10,
/* Bishop */	-10,   0,   0,   0,   0,   0,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,   0,   0,   0,   0,   0, -10,
		-10, -10, -10, -10, -10, -10, -10, -10},
	{	-10, -10, -10, -10, -10, -10, -10, -10,
/* Rook */	-10,   0,   0,   0,   0,   0,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,   0,   0,   0,   0,   0, -10,
		-10, -10, -10, -10, -10, -10, -10, -10},
	{	-10, -10, -10, -10, -10, -10, -10, -10,
/* Queen */	-10,   0,   0,   0,   0,   0,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  20,  20,  10,   0, -10,
		-10,   0,  10,  10,  10,  10,   0, -10,
		-10,   0,   0,   0,   0,   0,   0, -10,
		-10, -10, -10, -10, -10, -10, -10, -10},
	{	-10,  0,  0,  0,  0,  0,  0, -10,
/* King */	  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		  0, 15, 15, 15, 15, 15, 15,   0,
		-10,  0,  0,  0,  0,  0,  0, -10}
	};

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

	public static final int NOT_MATE = 1;
	public static final int CHECK_MATE = 2;
	public static final int STALE_MATE = 3;

	public static final int MOVE_ALL = 0;
	public static final int MOVE_CAPTURE = 1;
	public static final int MOVE_MOVE = 2;
	public static final int MOVE_PLACE = 3;

	public static final int ZBOX_SIZE = 781;
	public static final int WTM_HASH = 780;
	public static final int HOLD_START = 768;

	public static long[] hashBox = new long[ZBOX_SIZE];
	public static long startHash;

	private long key;
	
	public GenBoard()
	{
		square = new int[64];
		piece = new int[32];

		reset();
	}

	public GenBoard(final GenBoard board)
	{
		square = IntArray.clone(board.square);
		piece = IntArray.clone(board.piece);

		stm = board.stm;
		ply = board.ply;
		key = board.key;
	}

	private int pieceIndex(final int loc)
	{
		for (int i = 0; i < 32; i++)
			if (piece[i] == loc)
				return i;
		return Piece.NONE;
	}

	private int pieceIndex(final int loc, final int type)
	{
		final int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};
		final int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];

		for (int i = start; i < end; i++) {
			if (piece[i] == loc)
				return i;
		}
		return Piece.NONE;
	}

	public final void reset()
	{
		for (int i = 0; i < 64; i++)
			square[i] = Piece.EMPTY;
		for (int i = 0; i < 32; i++)
			piece[i] = Piece.PLACEABLE;

		ply = 0;
		stm = Piece.WHITE;
		key = startHash;
	}

	public int getStm()
	{
		return stm;
	}

	public int getPly()
	{
		return ply;
	}

	public long hash()
	{
		return key;
	}

	public int kingIndex(final int color)
	{
		return (Piece.WHITE == color)? piece[31] : piece[15];
	}

	public int[] getPieceCounts(final int Loc)
	{
		final int[] counts = new int[13];

		for (int i = 0; i < 32; i++) {
			if (piece[i] == Loc)
				counts[pieceType[i] + 6]++;
		}
		return counts;
	}

	public int[] getBoardArray()
	{
		return square;
	}

	public void make(final GenMove move)
	{
		// update board information
		square[move.to] = pieceType[move.index];
		if (move.from != Piece.PLACEABLE)
				square[move.from] = Piece.EMPTY;
		// update piece information
		piece[move.index] = move.to;
		if (move.xindex != Piece.NONE)
			piece[move.xindex] = Piece.DEAD;

		key += (stm == Piece.WHITE)? -hashBox[WTM_HASH] : hashBox[WTM_HASH];
		key += hashBox[12 * move.to + typeLookup[move.index]];
		if (move.from != Piece.PLACEABLE)
			key -= hashBox[12 * move.from + typeLookup[move.index]];
		else
			key -= hashBox[HOLD_START + typeLookup[move.index]];
		if (move.xindex != Piece.NONE)
			key -= hashBox[12 * move.to + typeLookup[move.xindex]];

		stm ^= -2;
		ply++;
	}

	public void unmake(final GenMove move)
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

		key += (stm == Piece.WHITE)? -hashBox[WTM_HASH] : hashBox[WTM_HASH];
		key -= hashBox[12 * move.to + typeLookup[move.index]];
		if (move.from != Piece.PLACEABLE)
			key += hashBox[12 * move.from + typeLookup[move.index]];
		else
			key += hashBox[HOLD_START + typeLookup[move.index]];
		if (move.xindex != Piece.NONE)
			key += hashBox[12 * move.to + typeLookup[move.xindex]];

		stm ^= -2;
		ply--;
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

	public boolean validMove(final GenMove moveIn, final GenMove move)
	{
		move.set(moveIn);

		if ((move.index = pieceIndex(move.from, pieceType[move.index])) == Piece.NONE)
			return false;
		if (pieceType[move.index] * stm <= 0)
			return false;
		if (move.xindex != Piece.NONE) {
			if ((move.xindex = pieceIndex(move.to, pieceType[move.xindex])) == Piece.NONE)
				return false;
		} else if (square[move.to] != Piece.EMPTY) {
			return false;
		}

		if (move.from != Piece.PLACEABLE && !fromto(move.from, move.to))
				return false;
		if (ply < 2 && Math.abs(pieceType[move.index]) != Piece.KING)
			return false;

		boolean ret = true;

		make(move);
		if (incheck(stm ^ -2))
			ret = false;
		if (move.from == Piece.PLACEABLE && incheck(stm))
			ret = false;
		unmake(move);

		return ret;
	}

	public int validMove(final GenMove move)
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

		if (move.from != Piece.PLACEABLE && !fromto(move.from, move.to))
				return INVALID_MOVEMENT;
		int ret = VALID_MOVE;

		make(move);
		// curr is opponent after make
		if (incheck(stm ^ -2))
			ret = IN_CHECK;
		else if (move.from == Piece.PLACEABLE && incheck(stm))
			ret = IN_CHECK_PLACE;
		unmake(move);

		return ret;
	}

	public int getNumMoves(final int color)
	{
		final GenMove move = new GenMove();
		int num = 0;

		// we must place king first
		if (ply < 2) {
			final int idx = pieceIndex(Piece.PLACEABLE, Piece.KING * color);

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
		final int start = (color == Piece.BLACK)? 0:16, end = (color == Piece.BLACK)? 16:32;
		for (int idx = start; idx < end; idx++) {
			if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
				continue;
			int n = 0;
			final int[] loc = genAll(piece[idx]);
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
			final int idx = pieceIndex(Piece.PLACEABLE, type * color);
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

	public GenMoveList getMoveList(final int color)
	{
		final GenMoveList data = new GenMoveList();

		data.size = 0;
		// we must place king first
		if (ply < 2) {
			final int idx = pieceIndex(Piece.PLACEABLE, Piece.KING * color);

			for (int loc = 0; loc < 64; loc++) {
				if (square[loc] != Piece.EMPTY)
					continue;
				final GenMoveNode item = new GenMoveNode();
				item.move.to = loc;
				item.move.index = idx;
				item.move.xindex = Piece.NONE;
				item.move.from = Piece.PLACEABLE;

				make(item.move);
				// place moves are only valid if neither side is inCheck
				if (!incheck(color) && !incheck(color ^ -2)) {
					// item.check initialized to false
					item.score = eval();
					data.list[data.size++] = item;
				}
				unmake(item.move);
			}
			return data;
		}
		// generate piece moves
		final int start = (color == Piece.BLACK)? 15:31, end = (color == Piece.BLACK)? 0:16;
		for (int idx = start; idx >= end; idx--) {
			if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
				continue;
			final int[] loc = genAll(piece[idx]);
			int n = 0;
			while (loc[n] != -1) {
				final GenMoveNode item = new GenMoveNode();
				item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
				item.move.to = loc[n];
				item.move.from = piece[idx];
				item.move.index = idx;

				make(item.move);
				if (!incheck(color)) {
					item.check = incheck(color ^ -2);
					item.score = eval();
					data.list[data.size++] = item;
				}
				unmake(item.move);
				n++;
			}
		}
		// generate piece place moves
		for (int type = Piece.QUEEN; type >= Piece.PAWN; type--) {
			final int idx = pieceIndex(Piece.PLACEABLE, type * color);
			if (idx == Piece.NONE)
				continue;
			for (int loc = 0; loc < 64; loc++) {
				if (square[loc] != Piece.EMPTY)
					continue;
				final GenMoveNode item = new GenMoveNode();
				item.move.index = idx;
				item.move.to = loc;
				item.move.xindex = Piece.NONE;
				item.move.from = Piece.PLACEABLE;

				make(item.move);
				// place moves are only valid if neither side is inCheck
				if (!incheck(color) && !incheck(color ^ -2)) {
					// item.check initialized to false
					item.score = eval();
					data.list[data.size++] = item;
				}
				unmake(item.move);
			}
		}
		return data;
	}

	public GenMoveList getMoveList(final int color, final int movetype)
	{
		final GenMoveList data = new GenMoveList();
		int start, end;

		data.size = 0;
		switch (movetype) {
		case MOVE_ALL:
			if (ply < 2) {
				final int idx = pieceIndex(Piece.PLACEABLE, Piece.KING * color);
				for (int loc = 0; loc < 64; loc++) {
					if (square[loc] != Piece.EMPTY)
						continue;
					final GenMoveNode item = new GenMoveNode();
					item.move.to = loc;
					item.move.index = idx;
					item.move.xindex = Piece.NONE;
					item.move.from = Piece.PLACEABLE;

					make(item.move);
					// place moves are only valid if neither side is inCheck
					if (!incheck(color) && !incheck(color ^ -2)) {
						// item.check initialized to false
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
				}
				break;
			}
			for (int type = Piece.QUEEN; type >= Piece.PAWN; type--) {
				final int idx = pieceIndex(Piece.PLACEABLE, type * color);
				if (idx == Piece.NONE)
					continue;
				for (int loc = 0; loc < 64; loc++) {
					if (square[loc] != Piece.EMPTY)
						continue;
					final GenMoveNode item = new GenMoveNode();
					item.move.index = idx;
					item.move.to = loc;
					item.move.xindex = Piece.NONE;
					item.move.from = Piece.PLACEABLE;

					make(item.move);
					// place moves are only valid if neither side is inCheck
					if (!incheck(color) && !incheck(color ^ -2)) {
						// item.check initialized to false
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
				}
			}
			start = (color == Piece.BLACK)? 15:31;
			end = (color == Piece.BLACK)? 0:16;
			for (int idx = start; idx >= end; idx--) {
				if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
					continue;
				final int[] loc = genAll(piece[idx]);
				int n = 0;
				while (loc[n] != -1) {
					final GenMoveNode item = new GenMoveNode();
					item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
					item.move.to = loc[n];
					item.move.from = piece[idx];
					item.move.index = idx;

					make(item.move);
					if (!incheck(color)) {
						item.check = incheck(color ^ -2);
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
					n++;
				}
			}
			break;
		case MOVE_CAPTURE:
			start = (color == Piece.BLACK)? 15:31;
			end = (color == Piece.BLACK)? 0:16;
			for (int idx = start; idx >= end; idx--) {
				if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
					continue;
				final int[] loc = genCapture(piece[idx]);
				int n = 0;
				while (loc[n] != -1) {
					final GenMoveNode item = new GenMoveNode();
					item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
					item.move.to = loc[n];
					item.move.from = piece[idx];
					item.move.index = idx;

					make(item.move);
					if (!incheck(color)) {
						item.check = incheck(color ^ -2);
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
					n++;
				}
			}
			break;
		case MOVE_MOVE:
			start = (color == Piece.BLACK)? 15:31;
			end = (color == Piece.BLACK)? 0:16;
			for (int idx = start; idx >= end; idx--) {
				if (piece[idx] == Piece.PLACEABLE || piece[idx] == Piece.DEAD)
					continue;
				final int[] loc = genMove(piece[idx]);
				int n = 0;
				while (loc[n] != -1) {
					final GenMoveNode item = new GenMoveNode();
					item.move.xindex = (square[loc[n]] == Piece.EMPTY)? Piece.NONE : pieceIndex(loc[n], square[loc[n]]);
					item.move.to = loc[n];
					item.move.from = piece[idx];
					item.move.index = idx;

					make(item.move);
					if (!incheck(color)) {
						item.check = incheck(color ^ -2);
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
					n++;
				}
			}
			break;
		case MOVE_PLACE:
			if (ply < 2) {
				final int idx = pieceIndex(Piece.PLACEABLE, Piece.KING * color);
				for (int loc = 0; loc < 64; loc++) {
					if (square[loc] != Piece.EMPTY)
						continue;
					final GenMoveNode item = new GenMoveNode();
					item.move.to = loc;
					item.move.index = idx;
					item.move.xindex = Piece.NONE;
					item.move.from = Piece.PLACEABLE;

					make(item.move);
					// place moves are only valid if neither side is inCheck
					if (!incheck(color) && !incheck(color ^ -2)) {
						// item.check initialized to false
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
				}
				break;
			}
			for (int type = Piece.QUEEN; type >= Piece.PAWN; type--) {
				final int idx = pieceIndex(Piece.PLACEABLE, type * color);
				if (idx == Piece.NONE)
					continue;
				for (int loc = 0; loc < 64; loc++) {
					if (square[loc] != Piece.EMPTY)
						continue;
					final GenMoveNode item = new GenMoveNode();
					item.move.index = idx;
					item.move.to = loc;
					item.move.xindex = Piece.NONE;
					item.move.from = Piece.PLACEABLE;

					make(item.move);
					// place moves are only valid if neither side is inCheck
					if (!incheck(color) && !incheck(color ^ -2)) {
						// item.check initialized to false
						item.score = eval();
						data.list[data.size++] = item;
					}
					unmake(item.move);
				}
			}
			break;
		}
		return data;
	}

	public int eval()
	{
		int white = 0, black = 0;
		for (int b = 0, w = 16; b < 16; b++, w++) {
			switch (piece[b]) {
			default:
				black += locValue[pieceType[w]][piece[b]];
			case Piece.PLACEABLE:
				black += pieceValue[b];
				break;
			case Piece.DEAD:
				black -= pieceValue[b];
				break;
			}
			switch (piece[w]) {
			default:
				white += locValue[pieceType[w]][piece[w]];
			case Piece.PLACEABLE:
				white += pieceValue[b];
				break;
			case Piece.DEAD:
				white -= pieceValue[b];
				break;
			}
		}
		white -= black;
		return (stm == Piece.WHITE)? -white : white;
	}
}
