package com.chess.genesis;

import android.os.Bundle;
import android.os.Handler;

class Benchmark implements Runnable
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

		final GenMoveList ptr = gboard.getMoveList(gboard.getStm());

		long nodes = 0;
		for (int i = 1; i < ptr.size; i++) {
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
		final RegMoveList ptr = rboard.getMoveList(rboard.getStm(), Move.MOVE_ALL);

		long nodes = 0;
		for (int i = 1; i < ptr.size; i++) {
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

	public void run()
	{
		final long gnps = GenBench();
		final long rnps = RegBench();

		final Bundle bundle = new Bundle();
		bundle.putLong("rnps", rnps);
		bundle.putLong("gnps", gnps);

		handle.sendMessage(handle.obtainMessage(MSG, bundle));
	}
}
