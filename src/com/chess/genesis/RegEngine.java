package com.chess.genesis;

import android.os.Bundle;
import android.os.Handler;
import java.util.Arrays;
import java.util.Date;

class RegEngine implements Runnable
{
	public final static int MSG = 111;

	public static final int MIN_SCORE = -(Integer.MAX_VALUE - 4);
	public static final int MAX_SCORE = (Integer.MAX_VALUE - 4);
	public static final int CHECKMATE_SCORE = MIN_SCORE;
	public static final int STALEMATE_SCORE = 0;

	private final ObjectArray<RegMove> pvMove;
	private final ObjectArray<RegMove> captureKiller;
	private final ObjectArray<RegMove> moveKiller;
	private final BoolArray tactical;
	private final BoolArray ismate;
	private final RegTransTable tt;
	private final Handler handle;

	private RegBoard board;
	private RegMoveList curr;
	private int secT;
	private long endT;
	private boolean active;

	public RegEngine(final Handler handler)
	{
		secT = 4;
		active = false;
		handle = handler;
		tt = new RegTransTable(8);
		pvMove = new ObjectArray<RegMove>();
		captureKiller = new ObjectArray<RegMove>();
		moveKiller = new ObjectArray<RegMove>();
		tactical = new BoolArray();
		ismate = new BoolArray();
	}

	public void setBoard(final RegBoard _board)
	{
		board = new RegBoard(_board);
	}

	private void pickRandomMove()
	{
		final int score = curr.list[0].score;
		int end = 1;

		for (int i = 1; i < curr.size; i++) {
			if (curr.list[i].score != score) {
				end = i + 1;
				break;
			}
		}
		pvMove.set(0, curr.list[(int) Math.abs(Rand64.next() % end)].move);
	}

	private int Quiescence(int alpha, final int beta, final int depth)
	{
		final RegMoveList ptr = board.getMoveList(board.getStm(), tactical.get(depth)? RegBoard.MOVE_ALL : RegBoard.MOVE_CAPTURE);

		if (ptr.size == 0)
			return tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : -board.eval();

		int best = MIN_SCORE, score = -board.eval();
		final MoveFlags undoflags = board.getMoveFlags();

		if (score >= beta)
			return score;
		alpha = Math.max(alpha, score);

		Arrays.sort(ptr.list, 0, ptr.size);

		for (int n = 0; n < ptr.size; n++) {
			// set check for opponent
			tactical.set(depth + 1, ptr.list[n].check);

			board.make(ptr.list[n].move);
			score = -Quiescence(-beta, -alpha, depth + 1);
			board.unmake(ptr.list[n].move, undoflags);

			if (score >= beta)
				return score;
			best = Math.max(best, score);
			alpha = Math.max(alpha, score);
		}
		return best;
	}

	private boolean NegaMoveType(final Int alpha, final int beta, final Int best,
		final int depth, final int limit, final ObjectArray<RegMove> killer, final int type)
	{
		final RegMove move = new RegMove();
		final MoveFlags undoflags = board.getMoveFlags();

		best.val = MIN_SCORE;

		// Try Killer Move
		final RegMove kmove = killer.get(depth);
		if (kmove != null && board.validMove(kmove, move)) {
			ismate.set(depth, false);

			board.make(move);

			// set check for opponent
			tactical.set(depth + 1, board.incheck(board.getStm()));

			best.val = -NegaScout(-beta, -alpha.val, depth + 1, limit);
			board.unmake(move, undoflags);

			if (best.val >= beta) {
				tt.setItem(board.hash(), best.val, move, limit - depth, RegTransItem.CUT_NODE);
				return true;
			} else if (best.val > alpha.val) {
				alpha.val = best.val;
				pvMove.set(depth, move);
			}
		}
		// Try all of moveType Moves
		final RegMoveList ptr = board.getMoveList(board.getStm(), type);

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
			board.unmake(ptr.list[n].move, undoflags);

			best.val = Math.max(best.val, ptr.list[n].score);
			if (best.val >= beta) {
				killer.set(depth, ptr.list[n].move);
				tt.setItem(board.hash(), best.val, killer.get(depth), limit - depth, RegTransItem.CUT_NODE);
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
		final RegTransItem tt_item = new RegTransItem();
		final Int score = new Int();
		final RegMove move = new RegMove();
		final MoveFlags undoflags = board.getMoveFlags();

		int best = MIN_SCORE;

		ismate.set(depth, true);
		pvMove.set(depth, (new RegMove()).setNull());

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
				board.unmake(move, undoflags);

				if (best >= beta) {
					tt.setItem(board.hash(), best, move, limit - depth, RegTransItem.CUT_NODE);
					return best;
				} else if (best > alpha) {
					alpha = best;
					pvMove.set(depth, move);
				}
			}
		}
		} while (false);

		final Int Alpha = new Int(alpha);
		if (NegaMoveType(Alpha, beta, score, depth, limit, captureKiller, RegBoard.MOVE_CAPTURE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, moveKiller, RegBoard.MOVE_MOVE))
			return score.val;
		best = Math.max(best, score.val);

		if (ismate.get(depth))
			best = tactical.get(depth)? CHECKMATE_SCORE + board.getPly() : STALEMATE_SCORE;
		tt.setItem(board.hash(), best, pvMove.get(depth), limit - depth, (pvMove.get(depth).isNull())? RegTransItem.ALL_NODE : RegTransItem.PV_NODE);

		return best;
	}

	private void search(int alpha, final int beta, final int depth, final int limit)
	{
		curr = (curr != null)? curr : board.getMoveList(board.getStm(), RegBoard.MOVE_ALL);
		final MoveFlags undoflags = board.getMoveFlags();

		int b = beta;
		for (int n = 0; n < curr.size; n++) {
			tactical.set(depth + 1, curr.list[n].check);

			board.make(curr.list[n].move);
			curr.list[n].score = -NegaScout(-b, -alpha, depth + 1, limit);
			if (curr.list[n].score > alpha && curr.list[n].score < beta && n > 0)
				curr.list[n].score = -NegaScout(-beta, -alpha, depth + 1, limit);
			board.unmake(curr.list[n].move, undoflags);

			if (curr.list[n].score > alpha) {
				alpha = curr.list[n].score;
				pvMove.set(depth, curr.list[n].move);
				tt.setItem(board.hash(), alpha, pvMove.get(depth), limit - depth, RegTransItem.PV_NODE);
			}
			b = alpha + 1;
		}
		Arrays.sort(curr.list, 0, curr.size);
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
