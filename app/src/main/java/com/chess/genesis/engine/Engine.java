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

import java.util.*;

public abstract class Engine
{
	static final int MIN_SCORE = -(Integer.MAX_VALUE - 4);
	static final int MAX_SCORE = (Integer.MAX_VALUE - 4);
	static final int CHECKMATE_SCORE = MIN_SCORE;
	static final int STALEMATE_SCORE = 0;

	final BoolArray tactical = new BoolArray();
	final BoolArray isMate =  new BoolArray();
	final Rand64 rand = new Rand64();

	final ObjectArray<Move> pvMove = new ObjectArray<>(Move::new);
	final ObjectArray<Move> captureKiller = new ObjectArray<>(Move::new);
	final ObjectArray<Move> moveKiller = new ObjectArray<>(Move::new);
	final ObjectArray<Move> sMove = new ObjectArray<>(Move::new);

	final TransTable tt;
	final TransItem ttItem = new TransItem();
	final MoveListPool pool = BaseBoard.pool;

	final MoveFlags undoFlags = new MoveFlags();

	MoveList curr;
	Board board;
	long endT;
	boolean active = false;

	Engine(Board boardType)
	{
		tt = new TransTable(boardType, 8);
	}

	public static Engine create(Board board)
	{
		return board instanceof GenBoard ? new GenEngine(board) : new RegEngine(board);
	}

	public void setBoard(final Board _board)
	{
		board = _board.copy();
	}

	int Quiescence(int _alpha, int beta, int depth)
	{
		var ptr = board.getMoveList(board.getStm(), tactical.get(depth)? Board.MOVE_ALL : Board.MOVE_CAPTURE);

		if (ptr.size == 0) {
			pool.put(ptr);
			return tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : -board.eval();
		}
		board.getMoveFlags(undoFlags);

		var score = -board.eval();
		if (score >= beta) {
			pool.put(ptr);
			return score;
		}
		var best = MIN_SCORE;
		var alpha = Math.max(_alpha, score);
		Arrays.sort(ptr.list, 0, ptr.size);

		for (MoveNode node : ptr) {
			// set check for opponent
			tactical.set(depth + 1, node.check);

			board.make(node.move);
			score = -Quiescence(-beta, -alpha, depth + 1);
			board.unmake(node.move, undoFlags);

			if (score >= beta) {
				pool.put(ptr);
				return score;
			}
			best = Math.max(best, score);
			alpha = Math.max(alpha, score);
		}
		pool.put(ptr);
		return best;
	}

	abstract int NegaScout(int inAlpha, int beta, int depth, int inLimit);

	boolean NegaMoveType(Int alpha, int beta, Int best, int depth, int limit, ObjectArray<Move> killer, int type)
	{
		board.getMoveFlags(undoFlags);
		best.val = MIN_SCORE;

		// Try Killer Move
		var kMove = killer.get(depth);
		if (board.validMove(kMove, kMove)) {
			isMate.set(depth, false);

			board.make(kMove);

			// set check for opponent
			tactical.set(depth + 1, board.inCheck(board.getStm()));

			best.val = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(kMove, undoFlags);

			if (best.val >= beta) {
				tt.setItem(board.hash(), best.val, kMove, limit - depth, TransItem.CUT_NODE);
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.get(depth).set(kMove);
			}
		}
		// Try all of moveType Moves
		var ptr = board.getMoveList(board.getStm(), type);

		if (ptr.size == 0) {
			pool.put(ptr);
			return false;
		}
		Arrays.sort(ptr.list, 0, ptr.size);

		isMate.set(depth, false);
		var b = alpha.val + 1;
		for (var node : ptr) {
			board.make(node.move);

			// set check for opponent
			tactical.set(depth + 1, node.check);

			node.score = -NegaScout(-b, -alpha.val, depth + 1, limit);
			if (node.score > alpha.val && node.score < beta)
				node.score = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(node.move, undoFlags);

			best.val = Math.max(best.val, node.score);
			if (best.val >= beta) {
				killer.get(depth).set(node.move);
				tt.setItem(board.hash(), best.val, killer.get(depth), limit - depth, TransItem.CUT_NODE);
				pool.put(ptr);
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.get(depth).set(node.move);
			}
			b = alpha.val + 1;
		}
		pool.put(ptr);
		return false;
	}

	protected void search(int Alpha, int beta, int depth, int limit)
	{
		board.getMoveFlags(undoFlags);

		var alpha = Alpha;
		var b = beta;
		for (int n = 0; n < curr.size; n++) {
			var node = curr.list[n];
			tactical.set(depth + 1, node.check);

			board.make(node.move);
			node.score = -NegaScout(-b, -alpha, depth + 1, limit);
			if (node.score > alpha && node.score < beta && n > 0)
				node.score = -NegaScout(-beta, -alpha, depth + 1, limit);
			board.unmake(node.move, undoFlags);

			if (node.score > alpha) {
				alpha = node.score;
				pvMove.get(depth).set(node.move);
				tt.setItem(board.hash(), alpha, pvMove.get(depth), limit - depth, TransItem.PV_NODE);
			}
			b = alpha + 1;
		}
		Arrays.sort(curr.list, 0, curr.size);
		pruneWeakMoves();
	}

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
		curr = board.getMoveList(board.getStm(), Board.MOVE_ALL);
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
