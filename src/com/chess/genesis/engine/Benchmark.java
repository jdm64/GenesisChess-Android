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

import android.os.*;

public class Benchmark implements Runnable
{
	public final static int MSG = 118;

	private final Handler handle;
	private final RegBoard rboard;
	private final GenBoard gboard;
	private long start;
	private long end;
	private long tNodes;

	public Benchmark(final Handler handler)
	{
		handle = handler;

		rboard = new RegBoard();
		gboard = new GenBoard();
	}

	private long GenPerft(final int depth)
	{
		if (depth == 0 || System.currentTimeMillis() > end)
			return 1;

		final MoveList ptr = gboard.getMoveList(gboard.getStm());

		long nodes = 0;
		for (int i = 0; i < ptr.size; i++) {
			gboard.make(ptr.list[i].move);
			nodes += GenPerft(depth - 1);
			gboard.unmake(ptr.list[i].move);
		}
		return nodes;
	}

	private long RegPerft(final int depth)
	{
		if (depth == 0 || System.currentTimeMillis() > end)
			return 1;

		final MoveFlags flags = rboard.getMoveFlags();
		final MoveList ptr = rboard.getMoveList(rboard.getStm(), Move.MOVE_ALL);

		long nodes = 0;
		for (int i = 0; i < ptr.size; i++) {
			rboard.make(ptr.list[i].move);
			nodes += RegPerft(depth - 1);
			rboard.unmake(ptr.list[i].move, flags);
		}
		return nodes;
	}

	private long GenBench()
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

	private long RegBench()
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

	@Override
	public synchronized void run()
	{
		final long gnps = GenBench();
		final long rnps = RegBench();

		final Bundle bundle = new Bundle();
		bundle.putLong("rnps", rnps);
		bundle.putLong("gnps", gnps);

		handle.sendMessage(handle.obtainMessage(MSG, bundle));
	}
}
