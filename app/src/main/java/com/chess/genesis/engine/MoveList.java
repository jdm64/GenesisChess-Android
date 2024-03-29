/* GenChess, a genesis chess engine
 * Copyright (C) 2014, Justin Madru (justin.jdm64@gmail.com)
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

import androidx.annotation.*;

public class MoveList implements Iterable<MoveNode>
{
	public final MoveNode[] list;
	private final MoveListIter iter = new MoveListIter(this);
	public int size;

	public MoveList()
	{
		size = 0;
		list = new MoveNode[320];
		for (int i = 0; i < 320; i++) {
			list[i] = new MoveNode();
		}
	}

	public void add(final MoveNode move)
	{
		list[size++].set(move);
	}

	@NonNull
	@Override
	public MoveListIter iterator()
	{
		iter.reset();
		return iter;
	}
}

