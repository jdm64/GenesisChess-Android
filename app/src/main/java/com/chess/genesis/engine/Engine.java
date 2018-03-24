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

import android.os.*;
import com.chess.genesis.util.*;

abstract class Engine implements Runnable
{
	static final int MIN_SCORE = -(Integer.MAX_VALUE - 4);
	private static final int MAX_SCORE = (Integer.MAX_VALUE - 4);
	static final int CHECKMATE_SCORE = MIN_SCORE;
	static final int STALEMATE_SCORE = 0;

	private final Handler handle;
	final BoolArray tactical;
	final BoolArray ismate;
	private final Rand64 rand;

	final ObjectArray<Move> pvMove;
	final ObjectArray<Move> captureKiller;
	final ObjectArray<Move> moveKiller;
	final ObjectArray<Move> smove;

	final TransTable tt;
	final TransItem ttItem;
	final MoveListPool pool;
	MoveList curr;
	Board board;

	private int secT;
	long endT;
	private boolean active;

	Engine(final Handler handler, final Board boardType)
	{
		secT = 4;
		active = false;
		handle = handler;
		tactical = new BoolArray();
		ismate = new BoolArray();
		rand = new Rand64();
		final NewInstance<Move> moveType = boardType.moveGenerator();

		smove = new ObjectArray<>(moveType);
		pvMove = new ObjectArray<>(moveType);
		captureKiller = new ObjectArray<>(moveType);
		moveKiller = new ObjectArray<>(moveType);
		ttItem = new TransItem(moveType);
		tt = new TransTable(boardType, 8);
		pool = boardType.getMoveListPool();
	}

	public void setBoard(final Board _board)
	{
		board = _board.clone();
	}

	protected abstract int getMsgId();
	protected abstract void search(int minScore, int maxScore, int i, int depth);

	private void pickRandomMove()
	{
		final int score = curr.list[0].score;
		int end = curr.size;

		for (int i = 1; i < curr.size; i++) {
			if (curr.list[i].score == score)
				continue;
			end = i;
			break;
		}
		final int ind = (int) (Math.abs(rand.next()) % end);
		pvMove.get(0).set(curr.list[ind].move);
	}

	void pruneWeakMoves()
	{
		if (curr.list[0].score == curr.list[curr.size - 1].score)
			return;

		int cut = curr.size;
		final int weak = curr.list[cut - 1].score;

		for (int i = cut - 2; i > 0; i--) {
			if (curr.list[i].score == weak)
				continue;
			cut = i + 1;
			break;
		}
		curr.size = cut;
	}

	public void stop()
	{
		endT = 0;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setTime(final int time)
	{
		if (time > 30)
			secT = 30;
		else if (time < 1)
			secT = 1;
		else
			secT = time;
	}

	public int getTime()
	{
		return secT;
	}

	@Override
	public synchronized void run()
	{
		active = true;
		endT = System.currentTimeMillis() + secT * 1000;
		curr = board.getMoveList(board.getStm(), Move.MOVE_ALL);

		for (int depth = 1; true; depth++) {
			search(MIN_SCORE, MAX_SCORE, 0, depth);
			if (System.currentTimeMillis() > endT)
				break;
		}

		// Randomize opening
		if (board.getPly() < 7)
			pickRandomMove();
		pool.put(curr);

		final Bundle bundle = new Bundle();
		bundle.putParcelable("move", pvMove.get(0));
		bundle.putLong("time", endT);

		handle.sendMessage(handle.obtainMessage(getMsgId(), bundle));
		active = false;
	}
}
