package com.chess.genesis;

class DistDB
{
	public final static DistDB[] TABLE = {
		new DistDB( 0,  Piece.EMPTY), new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK),
		new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK),
		new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(14, Piece.KNIGHT),
		new DistDB(15, Piece.BISHOP), new DistDB(16,   Piece.ROOK), new DistDB(17, Piece.BISHOP),
		new DistDB(18, Piece.KNIGHT), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB(31, Piece.KNIGHT), new DistDB(16,   Piece.ROOK),
		new DistDB(33, Piece.KNIGHT), new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(17, Piece.BISHOP),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(16,   Piece.ROOK),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(17, Piece.BISHOP) };

	public DistDB(final int Step, final int Type)
	{
		step = Step;
		type = Type;
	}

	public int step;
	public int type;
}
