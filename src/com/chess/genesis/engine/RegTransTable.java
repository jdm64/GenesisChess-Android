package com.chess.genesis;

class RegTransTable {

	private final RegTransItem[] table;
	private final int size;

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

	public void clear()
	{
		for (int i = 0; i < size; i++)
			table[i].hash = 0;
	}

	public boolean getItem(final long hash, final RegTransItem item)
	{
		item.set(table[(int) Math.abs(hash % size)]);
		return (item.hash == hash);
	}

	public void setItem(final long hash, final int score, final RegMove move, final int depth, final int type)
	{
		final int index = (int) Math.abs(hash % size);
		final RegTransItem item = table[index];

		item.hash = hash;
		item.score = score;
		item.depth = depth;
		item.type = type;
		item.move.set(move);

		table[index] = item;
	}
}
