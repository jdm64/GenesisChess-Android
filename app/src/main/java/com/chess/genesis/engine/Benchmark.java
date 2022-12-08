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

	private final MoveFlags flags = new MoveFlags();
	private final MoveListPool pool = BaseBoard.pool;

	private long end;

	private long perft(int depth, Board board)
	{
		if (depth == 0 || System.currentTimeMillis() > end)
			return 1;

		board.getMoveFlags(flags);
		var ptr = board.getMoveList(board.getStm(), Board.MOVE_ALL);

		var nodes = 0;
		for (MoveNode node : ptr) {
			board.make(node.move);
			nodes += perft(depth - 1, board);
			board.unmake(node.move, flags);
		}
		pool.put(ptr);
		return nodes;
	}

	public long run(Board board)
	{
		var now = System.currentTimeMillis();
		var tNodes = 0;
		var start = now;

		end = start + 5000;

		for (int i = 1; true; i++) {
			tNodes += perft(i, board);

			now = System.currentTimeMillis();
			if (now > end)
				break;
		}
		return (1000L * tNodes) / (now - start);
	}
}
