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

package com.chess.genesis;

class RegMoveNode implements Comparable<RegMoveNode>
{
	public RegMove move;
	public int score;
	public boolean check;

	public RegMoveNode()
	{
		score = 0;
		check = false;
		move = new RegMove();
	}

	public RegMoveNode(final RegMoveNode node)
	{
		score = node.score;
		check = node.check;
		move = new RegMove(node.move);
	}
 
	public int compareTo(final RegMoveNode a)
	{
		return (a.score > score)? 1 : ((a.score < score)? -1 : 0);
	}
}
