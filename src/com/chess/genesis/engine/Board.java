package com.chess.genesis;

interface Board
{
	void reset();

	int Piece(int index);
	int PieceType(int index);

	int getStm();
	MoveFlags getMoveFlags();

	int[] getBoardArray();
	int[] getPieceCounts(final int Loc);

	int kingIndex(final int color);
	boolean incheck(final int color);
	int isMate();

	String printZfen();

	void make(final GenMove move);
	void make(final RegMove move);

	void unmake(final GenMove move);
	void unmake(final RegMove move, final MoveFlags undoFlags);

	int validMove(final GenMove move);
	int validMove(final RegMove move);

	boolean validMove(final GenMove moveIn, final GenMove move);
	boolean validMove(final RegMove moveIn, final RegMove move);
}
