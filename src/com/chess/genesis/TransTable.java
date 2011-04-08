package com.chess.genesis;

class TransTable {

	private final TransItem[] table;
	private final int size;

	public TransTable(final int num_MB)
	{
		final Rand64 rad = new Rand64();

		for (int i = 0; i < Board.ZBOX_SIZE; i++)
			Board.hashBox[i] = rad.next();
		Board.startHash = rad.next();
		for (int i = Board.HOLD_START; i < Board.ZBOX_SIZE; i++)
			Board.startHash += Board.hashBox[i];

		size = (num_MB * 1048576) / 288;
		table = new TransItem[size];
		for (int i = 0; i < size; i++)
			table[i] = new TransItem();
	}

	public void clear()
	{
		for (int i = 0; i < size; i++)
			table[i].hash = 0;
	}

	public boolean getItem(final long hash, final TransItem item)
	{
		item.set(table[(int) Math.abs(hash % size)]);
		return (item.hash == hash);
	}

	public void setItem(final long hash, final int score, final Move move, final int depth, final int type)
	{
		final int index = (int) Math.abs(hash % size);
		final TransItem item = table[index];

		if (depth < item.depth)
			return;

		item.hash = hash;
		item.score = score;
		item.depth = depth;
		item.type = type;
		item.move.set(move);

		table[index] = item;
	}
}
