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

class MoveList
{
	public final MoveNode[] list;
	public int size;

	public MoveList(final Move move)
	{
		size = 0;
		list = new MoveNode[320];
		for (int i = 0; i < 320; i++) {
			list[i] = new MoveNode(move.newInstance());
		}
	}

	public void add(final MoveNode move)
	{
		list[size++].set(move);
	}
}

class MoveListPool
{
	private final LinkedList<MoveList> pool = new LinkedList<MoveList>();
	private final Move move;

	public MoveListPool(final Move moveType)
	{
		move = moveType;
	}

	public MoveList get()
	{
		if (pool.size() < 1)
			return new MoveList(move);
		synchronized (pool) {
			return pool.removeFirst();
		}
	}

	public void put(final MoveList item)
	{
		synchronized (pool) {
			pool.addFirst(item);
		}
	}
}
