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

class RegTransTable extends TransTable
{
	public RegTransTable(final int num_MB)
	{
		final Rand64 rad = new Rand64();

		for (int i = 0; i < RegBoard.ZBOX_SIZE; i++)
			RegBoard.hashBox[i] = rad.next();
		RegBoard.startHash = rad.next();
		for (int i = RegBoard.HOLD_START; i < RegBoard.ZBOX_SIZE; i++)
			RegBoard.startHash += RegBoard.hashBox[i];

		size = (num_MB * 1048576) / 288;
		table = new RegTransItem[size];
		for (int i = 0; i < size; i++)
			table[i] = new RegTransItem();
	}
}
