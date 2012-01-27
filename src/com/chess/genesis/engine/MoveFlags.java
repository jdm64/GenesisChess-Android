package com.chess.genesis;

class MoveFlags
{
	public int bits;

	public MoveFlags()
	{
		reset();
	}

	public MoveFlags(final MoveFlags flags)
	{
		bits = flags.bits;
	}

	public final void reset()
	{
		bits = 0xf0;
	}

	public int canEnPassant()
	{
		return bits & Move.CAN_EP;
	}

	public int enPassantFile()
	{
		return bits & Move.EP_FILE;
	}

	public void setEnPassant(final int file)
	{
		bits = (bits & ~0x0f) | (file | Move.CAN_EP);
	}

	public void clearEnPassant()
	{
		bits &= ~0x0f;
	}

	public int canCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? Move.W_CASTLE : Move.B_CASTLE);
	}

	public int canKingCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? Move.WK_CASTLE : Move.BK_CASTLE);
	}

	public int canQueenCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? Move.WQ_CASTLE : Move.BQ_CASTLE);
	}

	public void clearCastle(final int color)
	{
		bits &= ((color == Piece.WHITE)? ~Move.W_CASTLE : ~Move.B_CASTLE);
	}

	public void clearKingCastle(final int color)
	{
		bits &= ((color == Piece.WHITE)? ~Move.WK_CASTLE : ~Move.BK_CASTLE);
	}

	public void clearQueenCastle(final int color)
	{
		bits &= ((color == Piece.WHITE)? ~Move.WQ_CASTLE : ~Move.BQ_CASTLE);
	}

	public void setCastle(final int value)
	{
		bits &= (0xff & value);
	}
}
