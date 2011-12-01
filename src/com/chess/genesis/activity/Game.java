package com.chess.genesis;

import android.app.Activity;

public abstract class Game extends Activity
{
	public static Game self;
	public static ViewFlip3D game_board;
	public static boolean viewAsBlack = false;

	public abstract void displaySubmitMove();
	public abstract void reset();
}
