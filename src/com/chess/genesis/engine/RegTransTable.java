package com.chess.genesis;

class RegTransTable extends TransTable
{
	public RegTransTable(final int num_MB)
	{
		final Rand64 rad = new Rand64();

		for (int i = 0; i < RegBoard.ZBOX_SIZE; i++)
			RegBoard.hashBox[i] = rad.next();
		RegBoard.startHash = rad.next();
		for (int i = RegBoard.HOLD_START; i < RegBoard.ZBOX_SIZE; i++)
			RegBoard.startHash += RegBoard.hashBox[i];

		size = (num_MB * 1048576) / 288;
		table = new RegTransItem[size];
		for (int i = 0; i < size; i++)
			table[i] = new RegTransItem();
	}
}
