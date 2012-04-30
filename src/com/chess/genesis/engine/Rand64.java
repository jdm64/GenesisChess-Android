/*	GenChess, a genesis chess engine
	Copyright (C) 2012, Justin Madru (justin.jdm64@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.chess.genesis.engine;

import java.util.*;

class Rand64
{
	private static final int BLOCK = 8192;

	private final long[] box = new long[BLOCK];
	private final Random rand = new Random();

	private int size;
	private int last;

	public Rand64()
	{
		size = BLOCK - 1;
		last = BLOCK;

		for (int i = 0; i < BLOCK; i++)
			box[i] = i;
	}

	private long block()
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

	public long next()
	{
		long val = 0;

		for (int i = 0; i < 5; i++)
			val |= block() << (13 * i);
		return val;
	}
}
