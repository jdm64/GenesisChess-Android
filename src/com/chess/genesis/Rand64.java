package com.chess.genesis;

import java.util.Random;

class Rand64
{
	private static final int BLOCK = 8192;

	private static final long[] box = new long[BLOCK];;
	private static final Random rand = new Random();

	private static int size;
	private static int last;

	public Rand64()
	{
		size = BLOCK - 1;
		last = BLOCK;

		for (int i = 0; i < BLOCK; i++)
			box[i] = i;
	}

	private static long block()
	{
		int rn;

		if (size < 13)
			size = BLOCK - 1;
		do {
			rn = Math.abs(rand.nextInt() % size);
		} while (rn == last);
		last = rn;

		final long tmp = box[rn];
		box[rn] = box[size];
		box[size] = tmp;

		size--;
		return box[size + 1];
	}

	public static long next()
	{
		long val = 0;

		for (int i = 0; i < 5; i++)
			val |= block() << (13 * i);
		return val;
	}
};