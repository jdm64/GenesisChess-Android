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

public class Benchmark
{
	public final static String REG_NPS = "rnps";
	public final static String GEN_NPS = "gnps";

	private final RegBoard rboard;
	private final GenBoard gboard;
	private final MoveFlags flags;

	private MoveListPool pool;
	private long start;
	private long end;
	private long tNodes;

	public Benchmark()
	{
		rboard = new RegBoard();
		gboard = new GenBoard();
		flags = new MoveFlags();
		pool = BaseBoard.pool;
	}

	private long GenPerft(final int depth)
	{
		if (depth == 0 || System.currentTimeMillis() > end)
			return 1;

		var ptr = gboard.getMoveList(gboard.getStm(), Board.MOVE_ALL);

		long nodes = 0;
		for (final MoveNode node : ptr) {
			gboard.make(node.move);
			nodes += GenPerft(depth - 1);
			gboard.unmake(node.move);
		}
		pool.put(ptr);
		return nodes;
	}

	private long RegPerft(final int depth)
	{
		if (depth == 0 || System.currentTimeMillis() > end)
			return 1;

		rboard.getMoveFlags(flags);
		var ptr = rboard.getMoveList(rboard.getStm(), Board.MOVE_ALL);

		long nodes = 0;
		for (final MoveNode node : ptr) {
			rboard.make(node.move);
			nodes += RegPerft(depth - 1);
			rboard.unmake(node.move, flags);
		}
		pool.put(ptr);
		return nodes;
	}

	public long genBench()
	{
		long now = System.currentTimeMillis();

		tNodes = 0;
		start = now;
		end = start + 5000;

		for (int i = 1; true; i++) {
			tNodes += GenPerft(i);

			now = System.currentTimeMillis();
			if (now > end)
				break;
		}
		return (1000 * tNodes) / (now - start);
	}

	public long regBench()
	{
		long now = System.currentTimeMillis();

		tNodes = 0;
		start = now;
		end = start + 5000;

		for (int i = 1; true; i++) {
			tNodes += RegPerft(i);

			now = System.currentTimeMillis();
			if (now > end)
				break;
		}
		return (1000 * tNodes) / (now - start);
	}
}
