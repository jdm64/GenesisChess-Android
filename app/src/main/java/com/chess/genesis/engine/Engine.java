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

import java.util.function.*;

public abstract class Engine
{
	static final int MIN_SCORE = -(Integer.MAX_VALUE - 4);
	private static final int MAX_SCORE = (Integer.MAX_VALUE - 4);
	static final int CHECKMATE_SCORE = MIN_SCORE;
	static final int STALEMATE_SCORE = 0;

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
	long endT;
	boolean active;

	Engine(Board boardType)
	{
		active = false;
		tactical = new BoolArray();
		ismate = new BoolArray();
		rand = new Rand64();
		Supplier<Move> moveType = boardType.moveGenerator();

		smove = new ObjectArray<>(moveType);
		pvMove = new ObjectArray<>(moveType);
		captureKiller = new ObjectArray<>(moveType);
		moveKiller = new ObjectArray<>(moveType);
		ttItem = new TransItem(moveType);
		tt = new TransTable(boardType, 8);
		pool = boardType.getMoveListPool();
	}

	public static Engine create(Board board)
	{
		return board instanceof GenBoard ? new GenEngine(board) : new RegEngine(board);
	}

	public void setBoard(final Board _board)
	{
		board = _board.clone();
	}

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

	public boolean isActive()
	{
		return active;
	}

	public long getEndTime()
	{
		return endT;
	}

	public void setEndTime(long time)
	{
		endT = time;
	}

	private void think()
	{
		curr = board.getMoveList(board.getStm(), Move.MOVE_ALL);
		if (curr.size == 0)
			return;
		for (int depth = 1; true; depth++) {
			search(MIN_SCORE, MAX_SCORE, 0, depth);
			if (System.currentTimeMillis() > endT)
				break;
		}

		// Randomize opening
		if (board.getPly() < 7)
			pickRandomMove();
		pool.put(curr);
	}

	public Move getMove(int millisecond)
	{
		try {
			active = true;
			endT = System.currentTimeMillis() + millisecond;
			think();
			return pvMove.get(0);
		} finally {
			active = false;
		}
	}
}
