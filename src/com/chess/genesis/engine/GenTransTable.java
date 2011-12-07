package com.chess.genesis;

class GenTransTable extends TransTable
{
	public GenTransTable(final int num_MB)
	{
		final Rand64 rad = new Rand64();

		for (int i = 0; i < GenBoard.ZBOX_SIZE; i++)
			GenBoard.hashBox[i] = rad.next();
		GenBoard.startHash = rad.next();
		for (int i = GenBoard.HOLD_START; i < GenBoard.ZBOX_SIZE; i++)
			GenBoard.startHash += GenBoard.hashBox[i];

		size = (num_MB * 1048576) / 288;
		table = new GenTransItem[size];
		for (int i = 0; i < size; i++)
			table[i] = new GenTransItem();
	}
}
