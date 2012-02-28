package com.chess.genesis;

abstract class TransTable
{
	protected TransItem[] table;
	protected int size;

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

		item.hash = hash;
		item.score = score;
		item.depth = depth;
		item.type = type;
		item.move.set(move);

		table[index] = item;
	}
}
