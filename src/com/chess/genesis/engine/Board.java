package com.chess.genesis;

interface Board
{
	public void reset();

	public int Piece(int index);
	public int PieceType(int index);

	public int getStm();
	public MoveFlags getMoveFlags();

	public int[] getBoardArray();
	public int[] getPieceCounts(final int Loc);

	public int kingIndex(final int color);
	public boolean incheck(final int color);
	public int isMate();

	public String printZfen();

	public void make(final GenMove move);
	public void make(final RegMove move);

	public void unmake(final GenMove move);
	public void unmake(final RegMove move, final MoveFlags undoFlags);

	public int validMove(final GenMove move);
	public int validMove(final RegMove move);

	public boolean validMove(final GenMove moveIn, final GenMove move);
	public boolean validMove(final RegMove moveIn, final RegMove move);
}
