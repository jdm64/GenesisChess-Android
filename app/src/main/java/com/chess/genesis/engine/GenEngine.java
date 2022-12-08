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

public class GenEngine extends Engine
{
	private final ObjectArray<Move> placeKiller = new ObjectArray<>(Move::new);

	public GenEngine(Board boardType)
	{
		super(boardType);
	}

	@Override
	public int NegaScout(int inAlpha, int beta, int depth, int inLimit)
	{
		var limit = inLimit;
		if (System.currentTimeMillis() > endT) {
			return Quiescence(inAlpha, beta, depth);
		} else if (depth >= limit) {
			if (!tactical.get(depth))
				return Quiescence(inAlpha, beta, depth);
			limit++;
		}
		var score = new Int();
		board.getMoveFlags(undoFlags);

		var alpha = inAlpha;
		var best = MIN_SCORE;

		isMate.set(depth, true);
		pvMove.get(depth).setNull();

		do { // goto emulator

		// Try Transposition Table
		if (tt.getItem(board.hash(), ttItem)) {
			// Try score
			if (ttItem.getScore(alpha, beta, limit - depth, score))
				return score.val;

			// Try Move
			var move = sMove.get(depth);
			if (ttItem.getMove(move)) {
				if (!board.validMove(move, move))
					break;
				isMate.set(depth, false);

				board.make(move);

				// set check for opponent
				tactical.set(depth + 1, board.inCheck(board.getStm()));

				best = -NegaScout(-beta, -alpha, depth + 1, limit);
				board.unmake(move, undoFlags);

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

		var Alpha = new Int(alpha);
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
}
