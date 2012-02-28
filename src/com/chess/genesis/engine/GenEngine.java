package com.chess.genesis;

import android.os.Bundle;
import android.os.Handler;
import java.util.Arrays;
import java.util.Date;

class GenEngine extends Engine implements Runnable
{
	public final static int MSG = 109;

	private final ObjectArray<GenMove> pvMove;
	private final ObjectArray<GenMove> captureKiller;
	private final ObjectArray<GenMove> moveKiller;
	private final ObjectArray<GenMove> placeKiller;

	private GenBoard board;
	private GenMoveList curr;

	public GenEngine(final Handler handler)
	{
		super(handler);

		tt = new GenTransTable(8);
		pvMove = new ObjectArray<GenMove>();
		captureKiller = new ObjectArray<GenMove>();
		moveKiller = new ObjectArray<GenMove>();
		placeKiller = new ObjectArray<GenMove>();
	}

	public void setBoard(final GenBoard _board)
	{
		board = new GenBoard(_board);
	}

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
		final GenMoveList ptr = board.getMoveList(board.getStm(), tactical.get(depth)? Move.MOVE_ALL : Move.MOVE_CAPTURE);

		if (ptr.size == 0)
			return tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : -board.eval();

		int best = MIN_SCORE, score = -board.eval();

		if (score >= beta)
			return score;
		alpha = Math.max(alpha, score);

		Arrays.sort(ptr.list, 0, ptr.size);

		for (int n = 0; n < ptr.size; n++) {
			// set check for opponent
			tactical.set(depth + 1, ptr.list[n].check);

			board.make(ptr.list[n].move);
			score = -Quiescence(-beta, -alpha, depth + 1);
			board.unmake(ptr.list[n].move);

			if (score >= beta)
				return score;
			best = Math.max(best, score);
			alpha = Math.max(alpha, score);
		}
		return best;
	}

	private boolean NegaMoveType(final Int alpha, final int beta, final Int best,
		final int depth, final int limit, final ObjectArray<GenMove> killer, final int type)
	{
		final GenMove move = new GenMove();

		best.val = MIN_SCORE;

		// Try Killer Move
		final GenMove kmove = killer.get(depth);
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
		final GenMoveList ptr = board.getMoveList(board.getStm(), type);

		if (ptr.size == 0)
			return false;
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
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.set(depth, ptr.list[n].move);
			}
			b = alpha.val + 1;
		}
		return false;
	}

	private int NegaScout(int alpha, final int beta, final int depth, int limit)
	{
		if ((new Date()).getTime() > endT) {
			return Quiescence(alpha, beta, depth);
		} else if (depth >= limit) {
			if (!tactical.get(depth))
				return Quiescence(alpha, beta, depth);
			else
				limit++;
		}
		final GenTransItem tt_item = new GenTransItem();
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
		curr = (curr != null)? curr : board.getMoveList(board.getStm());

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

	public void run()
	{
		active = true;
		curr = null;
		endT = (new Date()).getTime() + secT * 1000;

		for (int depth = 1; true; depth++) {
			search(MIN_SCORE, MAX_SCORE, 0, depth);
			if ((new Date()).getTime() > endT)
				break;
		}

		// Randomize opening
		if (board.getPly() < 7)
			pickRandomMove();
		curr = null;

		final Bundle bundle = new Bundle();
		bundle.putParcelable("move", pvMove.get(0));
		bundle.putLong("time", endT);

		handle.sendMessage(handle.obtainMessage(MSG, bundle));
		active = false;
	}
}
