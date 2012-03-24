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

import android.os.Handler;

abstract class Engine implements Runnable
{
	public static final int MIN_SCORE = -(Integer.MAX_VALUE - 4);
	public static final int MAX_SCORE = (Integer.MAX_VALUE - 4);
	public static final int CHECKMATE_SCORE = MIN_SCORE;
	public static final int STALEMATE_SCORE = 0;

	protected Handler handle;
	protected BoolArray tactical;
	protected BoolArray ismate;
	protected TransTable tt;
	protected Rand64 rand;

	protected int secT;
	protected long endT;
	protected boolean active;

	public Engine(final Handler handler)
	{
		secT = 4;
		active = false;
		handle = handler;
		tactical = new BoolArray();
		ismate = new BoolArray();
		rand = new Rand64();
	}

	public abstract void setBoard(final RegBoard board);
	public abstract void setBoard(final GenBoard board);

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
}
