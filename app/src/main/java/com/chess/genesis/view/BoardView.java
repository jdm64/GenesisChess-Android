/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import com.chess.genesis.engine.*;

public class BoardView extends View implements OnClickListener, OnLongClickListener
{
	protected final static int WHITE = 0;
	protected final static int BLACK = 1;

	private final PieceImgPainter painter;
	private final BoardSquare[] squares = new BoardSquare[64];

	private GameState gamestate;
	private MotionEvent lastTouch;
	private int sqSize;
	private boolean viewAsBlack = false;

	public BoardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		painter = new PieceImgPainter(context);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	public void init(final GameState _gamestate, final boolean _viewAsBlack)
	{
		gamestate = _gamestate;
		viewAsBlack = _viewAsBlack;
		int x = 0;

		for (int i = 0; i < 64; i++) {
			squares[i] = new BoardSquare(this, painter, BaseBoard.SFF88(i));
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		final int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(size, size);

		sqSize = size / 8;
		painter.resize(sqSize);
		if (viewAsBlack) {
			for (int i = 0; i < 64; i++)
				squares[i].setXY(sqSize * (7 - (i % 8)), sqSize * (7 - (i / 8)));
		} else {
			for (int i = 0; i < 64; i++)
				squares[i].setXY(sqSize * (i % 8), sqSize * (i / 8));
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		for (int i = 0; i < 64; i++)
			squares[i].draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		lastTouch = event;
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View view)
	{
		IBoardSq sq = getTouchedSquare();
		if (sq != null)
			gamestate.boardClick(sq);
	}

	@Override
	public boolean onLongClick(View view)
	{
		IBoardSq sq = getTouchedSquare();
		if (sq != null) {
			gamestate.boardLongClick(sq);
			return true;
		}
		return false;
	}

	public IBoardSq getSquare(int index)
	{
		return squares[BaseBoard.EE64F(index)];
	}

	private IBoardSq getTouchedSquare()
	{
		final int[] loc = new int[2];
		getLocationInWindow(loc);

		int x = (int) Math.floor((lastTouch.getX() - loc[0]) / sqSize);
		int y = (int) Math.floor((lastTouch.getY() - loc[1]) / sqSize);
		int index = (8 * y + x);
		if (viewAsBlack)
			index = 63 - index;

		if (index < 0 || index >= 64)
			return null;
		return squares[index];
	}
}
