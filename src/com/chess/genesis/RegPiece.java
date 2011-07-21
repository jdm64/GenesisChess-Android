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

	public static RegPiece[] arrayCopy(final RegPiece[] arr)
	{
		final RegPiece[] copy = new RegPiece[arr.length];

		for (int i = 0; i < arr.length; i++)
			copy[i] = arr[i].clone();
		return copy;
	}
}
