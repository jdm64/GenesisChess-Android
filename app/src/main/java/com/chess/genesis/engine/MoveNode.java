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

import com.chess.genesis.util.*;

class MoveNode implements Comparable<MoveNode>
{
	public final Move move;
	public int score;
	public boolean check;

	public MoveNode(final NewInstance<Move> moveType)
	{
		score = 0;
		check = false;
		move = moveType.newInstance();
	}

	public MoveNode(final MoveNode node)
	{
		score = node.score;
		check = node.check;
		move = node.move.newInstance();
		move.set(node.move);
	}

	public void set(final MoveNode node)
	{
		score = node.score;
		check = node.check;
		move.set(node.move);
	}

	@Override
	public int compareTo(final MoveNode a)
	{
		return Integer.compare(a.score, score);
	}
}
