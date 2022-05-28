/* GenChess, a genesis chess engine
 * Copyright (C) 2022, Justin Madru (justin.jdm64@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chess.genesis.engine;

import java.util.*;

public class MoveListPool
{
	private final Queue<MoveList> pool = new ArrayDeque<>();
	private final NewInstance<Move> moveType;

	public MoveListPool(final NewInstance<Move> _moveType)
	{
		moveType = _moveType;
	}

	public MoveList get()
	{
		if (pool.size() < 1)
			return new MoveList(moveType);
		synchronized (pool) {
			return pool.remove();
		}
	}

	public void put(final MoveList item)
	{
		synchronized (pool) {
			pool.add(item);
		}
	}
}
