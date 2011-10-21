package com.chess.genesis;

class GenTransTable {

	private final GenTransItem[] table;
	private final int size;

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

	public void clear()
	{
		for (int i = 0; i < size; i++)
			table[i].hash = 0;
	}

	public boolean getItem(final long hash, final GenTransItem item)
	{
		item.set(table[(int) Math.abs(hash % size)]);
		return (item.hash == hash);
	}

	public void setItem(final long hash, final int score, final GenMove move, final int depth, final int type)
	{
		final int index = (int) Math.abs(hash % size);
		final GenTransItem item = table[index];

		item.hash = hash;
		item.score = score;
		item.depth = depth;
		item.type = type;
		item.move.set(move);

		table[index] = item;
	}
}
