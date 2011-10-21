package com.chess.genesis;

class Int
{
	public int val;

	public Int()
	{
		val = 0;
	}

	public Int(final int a)
	{
		val = a;
	}

	public Int(final Int a)
	{
		val = a.val;
	}
}
