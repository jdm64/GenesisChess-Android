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
