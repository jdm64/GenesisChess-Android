package com.chess.genesis;

import android.os.Bundle;
import android.os.Handler;
import java.util.Arrays;
import java.util.Date;

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
