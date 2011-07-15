package com.chess.genesis;

class RegPiece
{
	public int loc;
	public int type;
	
	public RegPiece(final int Loc, final int Type)
	{
		loc = Loc;
		type = Type;
	}

	public RegPiece clone()
	{
		return new RegPiece(loc, type);
	}
}
