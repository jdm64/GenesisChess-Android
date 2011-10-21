package com.chess.genesis;

class RegTransItem
{
	public static final int NONE_NODE = 0;
	public static final int ALL_NODE = 3;
	public static final int CUT_NODE = 6;
	public static final int PV_NODE = 7;

	public static final int HAS_SCORE = 2;
	public static final int HAS_MOVE = 4;

	public final RegMove move;

	public long hash;
	public int score;
	public int depth;
	public int type;

	public RegTransItem()
	{
		hash = 0;
		score = 0;
		depth = 0;
		type = NONE_NODE;
		move = new RegMove();
	}

	public void set(final RegTransItem item)
	{
		hash = item.hash;
		score = item.score;
		depth = item.depth;
		type = item.type;
		move.set(item.move);
	}

	public boolean getScore(final int alpha, final int beta, final int inDepth, final Int outScore)
	{
		if ((type & HAS_SCORE) != 0 && depth >= inDepth) {
			switch (type) {
			case PV_NODE:
				outScore.val = score;
				return true;
			case CUT_NODE:
				if (score >= beta) {
					outScore.val = score;
					return true;
				}
				break;
			case ALL_NODE:
				if (score <= alpha) {
					outScore.val = score;
					return true;
				}
				break;
			default:
				return false;
			}
		}
		return false;
	}

	public boolean getMove(final RegMove inMove)
	{
		if ((type & HAS_MOVE) != 0) {
			inMove.set(move);
			return true;
		} else {
			return false;
		}
	}
}
