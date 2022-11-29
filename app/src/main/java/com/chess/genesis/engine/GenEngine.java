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

public class GenEngine extends Engine
{
	private final ObjectArray<Move> placeKiller;

	public GenEngine(Board boardType)
	{
		super(boardType);
		placeKiller = new ObjectArray<>(Move::new);
	}

	private int Quiescence(final int _alpha, final int beta, final int depth)
	{
		var ptr = board.getMoveList(board.getStm(), tactical.get(depth)? Board.MOVE_ALL : Board.MOVE_CAPTURE);

		if (ptr.size == 0) {
			pool.put(ptr);
			return tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : -board.eval();
		}

		int score = -board.eval();
		if (score >= beta) {
			pool.put(ptr);
			return score;
		}
		int best = MIN_SCORE;
		int alpha = Math.max(_alpha, score);
		Arrays.sort(ptr.list, 0, ptr.size);

		for (final MoveNode node : ptr) {
			// set check for opponent
			tactical.set(depth + 1, node.check);

			board.make(node.move);
			score = -Quiescence(-beta, -alpha, depth + 1);
			board.unmake(node.move);

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
		best.val = MIN_SCORE;

		// Try Killer Move
		final Move kMove = killer.get(depth);
		if (board.validMove(kMove, kMove)) {
			isMate.set(depth, false);

			board.make(kMove);

			// set check for opponent
			tactical.set(depth + 1, board.inCheck(board.getStm()));

			best.val = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(kMove);

			if (best.val >= beta) {
				tt.setItem(board.hash(), best.val, kMove, limit - depth, TransItem.CUT_NODE);
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.get(depth).set(kMove);
			}
		}
		// Try all of moveType Moves
		final MoveList ptr = board.getMoveList(board.getStm(), type);

		if (ptr.size == 0) {
			pool.put(ptr);
			return false;
		}
		Arrays.sort(ptr.list, 0, ptr.size);

		isMate.set(depth, false);
		int b = alpha.val + 1;
		for (final MoveNode node : ptr) {
			board.make(node.move);

			// set check for opponent
			tactical.set(depth + 1, node.check);

			node.score = -NegaScout(-b, -alpha.val, depth + 1, limit);
			if (node.score > alpha.val && node.score < beta)
				node.score = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(node.move);

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

	private int NegaScout(final int inAlpha, final int beta, final int depth, final int inLimit)
	{
		int limit = inLimit;
		if (System.currentTimeMillis() > endT) {
			return Quiescence(inAlpha, beta, depth);
		} else if (depth >= limit) {
			if (!tactical.get(depth))
				return Quiescence(inAlpha, beta, depth);
			limit++;
		}
		final Int score = new Int();

		int alpha = inAlpha;
		int best = MIN_SCORE;

		isMate.set(depth, true);
		pvMove.get(depth).setNull();

		do { // goto emulator

		// Try Transposition Table
		if (tt.getItem(board.hash(), ttItem)) {
			// Try score
			if (ttItem.getScore(alpha, beta, limit - depth, score))
				return score.val;

			// Try Move
			final Move move = sMove.get(depth);
			if (ttItem.getMove(move)) {
				if (!board.validMove(move, move))
					break;
				isMate.set(depth, false);

				board.make(move);

				// set check for opponent
				tactical.set(depth + 1, board.inCheck(board.getStm()));

				best = -NegaScout(-beta, -alpha, depth + 1, limit);
				board.unmake(move);

				if (best >= beta) {
					tt.setItem(board.hash(), best, move, limit - depth, TransItem.CUT_NODE);
					return best;
				} else if (best > alpha) {
					alpha = best;
					pvMove.get(depth).set(move);
				}
			}
		}
		} while (false);

		final Int Alpha = new Int(alpha);
		if (NegaMoveType(Alpha, beta, score, depth, limit, captureKiller, Board.MOVE_CAPTURE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, moveKiller, Board.MOVE_MOVE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, placeKiller, Board.MOVE_PLACE))
			return score.val;
		best = Math.max(best, score.val);

		if (isMate.get(depth))
			best = tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : STALEMATE_SCORE;
		tt.setItem(board.hash(), best, pvMove.get(depth), limit - depth, (pvMove.get(depth).isNull())? TransItem.ALL_NODE : TransItem.PV_NODE);

		return best;
	}

	@Override
	protected void search(final int Alpha, final int beta, final int depth, final int limit)
	{
		int alpha = Alpha, b = beta;
		for (int n = 0; n < curr.size; n++) {
			tactical.set(depth + 1, curr.list[n].check);

			board.make(curr.list[n].move);
			curr.list[n].score = -NegaScout(-b, -alpha, depth + 1, limit);
			if (curr.list[n].score > alpha && curr.list[n].score < beta && n > 0)
				curr.list[n].score = -NegaScout(-beta, -alpha, depth + 1, limit);
			board.unmake(curr.list[n].move);

			if (curr.list[n].score > alpha) {
				alpha = curr.list[n].score;
				pvMove.get(depth).set(curr.list[n].move);
				tt.setItem(board.hash(), alpha, pvMove.get(depth), limit - depth, TransItem.PV_NODE);
			}
			b = alpha + 1;
		}
		Arrays.sort(curr.list, 0, curr.size);
		pruneWeakMoves();
	}
}
