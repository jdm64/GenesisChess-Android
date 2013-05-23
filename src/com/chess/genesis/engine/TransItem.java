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

import com.chess.genesis.util.*;

class TransItem
{
	public static final int NONE_NODE = 0;
	public static final int ALL_NODE = 3;
	public static final int CUT_NODE = 6;
	public static final int PV_NODE = 7;

	public static final int HAS_SCORE = 2;
	public static final int HAS_MOVE = 4;

	public final Move move;
	public long hash;
	public int score;
	public int depth;
	public int type;

	public TransItem(final NewInstance<Move> moveType)
	{
		hash = score = depth = 0;
		type = NONE_NODE;
		move = moveType.newInstance();
	}

	public void set(final TransItem item)
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

	public boolean getMove(final Move inMove)
	{
		if ((type & HAS_MOVE) != 0) {
			inMove.set(move);
			return true;
		}
		return false;
	}
}
