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

	public void reset()
	{
		bits = 0xf0;
	}

	public int canEnPassant()
	{
		return bits & 0x8;
	}

	public int enPassantFile()
	{
		return bits & 0x7;
	}

	public void setEnPassant(int file)
	{
		bits = (bits & ~0xf) | (file | 0x8);
	}

	public void clearEnPassant()
	{
		bits &= ~0xf;
	}

	public int canCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? 0x30 : 0xc0);
	}

	public int canKingCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? 0x10 : 0x40);
	}

	public int canQueenCastle(final int color)
	{
		return bits & ((color == Piece.WHITE)? 0x20 : 0x80);
	}

	void clearCastle(int color)
	{
		bits &= ((color == Piece.WHITE)? ~0x30 : ~0xc0);
	}

	void clearKingCastle(int color)
	{
		bits &= ((color == Piece.WHITE)? ~0x10 : ~0x40);
	}

	void clearQueenCastle(int color)
	{
		bits &= ((color == Piece.WHITE)? ~0x20 : ~0x80);
	}

	void setCastle(int value)
	{
		bits &= (0xff & value);
	}
}
