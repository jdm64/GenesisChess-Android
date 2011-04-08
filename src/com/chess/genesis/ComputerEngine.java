package com.chess.genesis;

import android.os.Handler;
import java.util.Arrays;

class ComputerEngine implements Runnable
{
	public final static int MSG = 109;

	public static final int MIN_SCORE = -(Integer.MAX_VALUE - 4);
	public static final int MAX_SCORE = (Integer.MAX_VALUE - 4);
	public static final int CHECKMATE_SCORE = MIN_SCORE;
	public static final int STALEMATE_SCORE = 0;

	public static final int maxNg = 3;

	private final ObjectArray<Move> pvMove;
	private final ObjectArray<Move> captureKiller;
	private final ObjectArray<Move> moveKiller;
	private final ObjectArray<Move> placeKiller;
	private final BoolArray tactical;
	private final BoolArray ismate;
	private final TransTable tt;
	private final Handler handle;

	private Board board;
	private MoveList curr;

	public ComputerEngine(final Handler handler)
	{
		handle = handler;
		tt = new TransTable(8);
		pvMove = new ObjectArray<Move>();
		captureKiller = new ObjectArray<Move>();
		moveKiller = new ObjectArray<Move>();
		placeKiller = new ObjectArray<Move>();
		tactical = new BoolArray();
		ismate = new BoolArray();
	}

	public void setBoard(final Board _board)
	{
		board = new Board(_board);
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
		final MoveList ptr = board.getMoveList(board.getStm(), tactical.get(depth)? Board.MOVE_ALL : Board.MOVE_CAPTURE);

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
		final int depth, final int limit, final ObjectArray<Move> killer, final int type)
	{
		final Move move = new Move();

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
		if (depth >= limit) {
			if (!tactical.get(depth))
				return Quiescence(alpha, beta, depth);
			else
				limit++;
		}
		final TransItem tt_item = new TransItem();
		final Int score = new Int();
		final Move move = new Move();

		int best = MIN_SCORE;

		ismate.set(depth, true);
		pvMove.set(depth, (new Move()).setNull());

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
		if (NegaMoveType(Alpha, beta, score, depth, limit, captureKiller, Board.MOVE_CAPTURE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, moveKiller, Board.MOVE_MOVE))
			return score.val;
		best = Math.max(best, score.val);
		if (NegaMoveType(Alpha, beta, score, depth, limit, placeKiller, Board.MOVE_PLACE))
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
	}

	public void run()
	{
		curr = null;
		for (int depth = 1; depth <= maxNg; depth++)
			search(MIN_SCORE, MAX_SCORE, 0, depth);

		// Randomize opening
		if (board.getPly() < 3)
			pickRandomMove();
		curr = null;

		handle.sendMessage(handle.obtainMessage(MSG, pvMove.get(0)));
	}
}
