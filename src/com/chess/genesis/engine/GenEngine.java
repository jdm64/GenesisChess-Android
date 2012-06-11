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
import com.chess.genesis.util.*;
import java.util.*;

public class GenEngine extends Engine
{
	public final static int MSG = 109;

	private final ObjectArray<Move> pvMove;
	private final ObjectArray<Move> captureKiller;
	private final ObjectArray<Move> moveKiller;
	private final ObjectArray<Move> placeKiller;

	private MoveListPool pool;
	private GenBoard board;
	private MoveList curr;

	public GenEngine(final Handler handler)
	{
		super(handler);

		tt = new TransTable(new GenBoard(), new GenMove(), 8);
		pvMove = new ObjectArray<Move>();
		captureKiller = new ObjectArray<Move>();
		moveKiller = new ObjectArray<Move>();
		placeKiller = new ObjectArray<Move>();
	}

	@Override
	public void setBoard(final GenBoard _board)
	{
		board = new GenBoard(_board);
		pool = board.getMoveListPool();
	}

	@Override
	public void setBoard(final RegBoard _board)
	{
		// NEVER CALL!
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
		pvMove.set(0, curr.list[ind].move);
	}

	private void pruneWeakMoves()
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

	private int Quiescence(int alpha, final int beta, final int depth)
	{
		final MoveList ptr = board.getMoveList(board.getStm(), tactical.get(depth)? Move.MOVE_ALL : Move.MOVE_CAPTURE);

		if (ptr.size == 0) {
			pool.put(ptr);
			return tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : -board.eval();
		}

		int best = MIN_SCORE, score = -board.eval();

		if (score >= beta) {
			pool.put(ptr);
			return score;
		}
		alpha = Math.max(alpha, score);

		Arrays.sort(ptr.list, 0, ptr.size);

		for (int n = 0; n < ptr.size; n++) {
			// set check for opponent
			tactical.set(depth + 1, ptr.list[n].check);

			board.make(ptr.list[n].move);
			score = -Quiescence(-beta, -alpha, depth + 1);
			board.unmake(ptr.list[n].move);

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

	private boolean NegaMoveType(final Int alpha, final int beta, final Int best,
		final int depth, final int limit, final ObjectArray<Move> killer, final int type)
	{
		final GenMove move = new GenMove();

		best.val = MIN_SCORE;

		// Try Killer Move
		final Move kmove = killer.get(depth);
		if (kmove != null && board.validMove(kmove, move)) {
			ismate.set(depth, false);

			board.make(move);

			// set check for opponent
			tactical.set(depth + 1, board.incheck(board.getStm()));

			best.val = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(move);

			if (best.val >= beta) {
				tt.setItem(board.hash(), best.val, move, limit - depth, TransItem.CUT_NODE);
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.set(depth, move);
			}
		}
		// Try all of moveType Moves
		final MoveList ptr = board.getMoveList(board.getStm(), type);

		if (ptr.size == 0) {
			pool.put(ptr);
			return false;
		}
		Arrays.sort(ptr.list, 0, ptr.size);

		ismate.set(depth, false);
		int b = alpha.val + 1;
		for (int n = 0; n < ptr.size; n++) {
			board.make(ptr.list[n].move);

			// set check for opponent
			tactical.set(depth + 1, ptr.list[n].check);

			ptr.list[n].score = -NegaScout(-b, -alpha.val, depth + 1, limit);
			if (ptr.list[n].score > alpha.val && ptr.list[n].score < beta)
				ptr.list[n].score = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(ptr.list[n].move);

			best.val = Math.max(best.val, ptr.list[n].score);
			if (best.val >= beta) {
				killer.set(depth, ptr.list[n].move);
				tt.setItem(board.hash(), best.val, killer.get(depth), limit - depth, TransItem.CUT_NODE);
				pool.put(ptr);
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.set(depth, ptr.list[n].move);
			}
			b = alpha.val + 1;
		}
		pool.put(ptr);
		return false;
	}

	private int NegaScout(int alpha, final int beta, final int depth, int limit)
	{
		if (new Date().getTime() > endT) {
			return Quiescence(alpha, beta, depth);
		} else if (depth >= limit) {
			if (!tactical.get(depth))
				return Quiescence(alpha, beta, depth);
			limit++;
		}
		final TransItem tt_item = new TransItem(new GenMove());
		final Int score = new Int();
		final GenMove move = new GenMove();

		int best = MIN_SCORE;

		ismate.set(depth, true);
		pvMove.set(depth, new GenMove().setNull());

		do { // goto emulator

		// Try Transposition Table
		if (tt.getItem(board.hash(), tt_item)) {
			// Try score
			if (tt_item.getScore(alpha, beta, limit - depth, score))
				return score.val;

			// Try Move
			if (tt_item.getMove(move)) {
				if (!board.validMove(move, move))
					break;
				ismate.set(depth, false);

				board.make(move);

				// set check for opponent
				tactical.set(depth + 1, board.incheck(board.getStm()));

				best = -NegaScout(-beta, -alpha, depth + 1, limit);
				board.unmake(move);

				if (best >= beta) {
					tt.setItem(board.hash(), best, move, limit - depth, TransItem.CUT_NODE);
					return best;
				} else if (best > alpha) {
					alpha = best;
					pvMove.set(depth, move);
				}
			}
		}
		} while (false);

		final Int Alpha = new Int(alpha);
		if (NegaMoveType(Alpha, beta, score, depth, limit, captureKiller, Move.MOVE_CAPTURE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, moveKiller, Move.MOVE_MOVE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, placeKiller, Move.MOVE_PLACE))
			return score.val;
		best = Math.max(best, score.val);

		if (ismate.get(depth))
			best = tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : STALEMATE_SCORE;
		tt.setItem(board.hash(), best, pvMove.get(depth), limit - depth, (pvMove.get(depth).isNull())? TransItem.ALL_NODE : TransItem.PV_NODE);

		return best;
	}

	private void search(int alpha, final int beta, final int depth, final int limit)
	{
		int b = beta;
		for (int n = 0; n < curr.size; n++) {
			tactical.set(depth + 1, curr.list[n].check);

			board.make(curr.list[n].move);
			curr.list[n].score = -NegaScout(-b, -alpha, depth + 1, limit);
			if (curr.list[n].score > alpha && curr.list[n].score < beta && n > 0)
				curr.list[n].score = -NegaScout(-beta, -alpha, depth + 1, limit);
			board.unmake(curr.list[n].move);

			if (curr.list[n].score > alpha) {
				alpha = curr.list[n].score;
				pvMove.set(depth, curr.list[n].move);
				tt.setItem(board.hash(), alpha, pvMove.get(depth), limit - depth, TransItem.PV_NODE);
			}
			b = alpha + 1;
		}
		Arrays.sort(curr.list, 0, curr.size);
		pruneWeakMoves();
	}

	@Override
	public synchronized void run()
	{
		active = true;
		endT = new Date().getTime() + secT * 1000;
		curr = board.getMoveList(board.getStm());

		for (int depth = 1; true; depth++) {
			search(MIN_SCORE, MAX_SCORE, 0, depth);
			if (new Date().getTime() > endT)
				break;
		}

		// Randomize opening
		if (board.getPly() < 7)
			pickRandomMove();
		pool.put(curr);

		final Bundle bundle = new Bundle();
		bundle.putParcelable("move", pvMove.get(0));
		bundle.putLong("time", endT);

		handle.sendMessage(handle.obtainMessage(MSG, bundle));
		active = false;
	}
}
