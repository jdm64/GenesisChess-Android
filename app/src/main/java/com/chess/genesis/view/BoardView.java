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
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;

public class BoardView extends View implements OnClickListener, OnLongClickListener
{
	protected final static int WHITE = 0;
	protected final static int BLACK = 1;

	private final PieceImgPainter painter;
	private final BoardSquare[] squares = new BoardSquare[64];

	private IGameController gameCtrl;
	private MotionEvent lastTouch;
	private int sqSize;
	private int halfSq;
	private boolean viewAsBlack = false;

	// For dragging pieces
	private boolean isDragging = false;
	private IBoardSq dragSq = null;
	private float downX, downY;
	private final PointF dragPos = new PointF();

	public BoardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		painter = new PieceImgPainter(context);
		setOnClickListener(this);
		setOnLongClickListener(this);

		for (int i = 0; i < 64; i++) {
			squares[i] = new BoardSquare(this, painter, Board.SFF88(i));
		}
	}

	public void init(IGameController _gameCtrl, boolean _viewAsBlack)
	{
		setController(_gameCtrl);
		setViewAsBlack(_viewAsBlack);
	}

	public void setController(IGameController _gameCtrl)
	{
		gameCtrl = _gameCtrl;
	}

	public void setViewAsBlack(boolean _viewAsBlack)
	{
		if (viewAsBlack == _viewAsBlack)
			return;
		viewAsBlack = _viewAsBlack;
		setXY();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		final int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(size, size);

		sqSize = size / 8;
		halfSq = sqSize / 2;
		painter.resize(sqSize);
		setXY();
	}

	private void setXY()
	{
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
		for (int i = 0; i < 64; i++) {
			var sq = squares[i];
			sq.draw(canvas, !isDragging || sq != dragSq);
		}
		if (isDragging && dragSq != null) {
			painter.offsetTo((int) dragPos.x - halfSq, (int) dragPos.y - halfSq);
			painter.drawPiece(canvas, dragSq.getPiece());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		lastTouch = event;
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = x;
			downY = y;
			isDragging = false;
			var touchSq = getTouchedSquare();
			if (touchSq != null) {
				if (!touchSq.isHighlighted()) {
					performClick();
				}
				if (touchSq.getPiece() != Piece.EMPTY) {
					dragSq = touchSq;
				}
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (!isDragging && dragSq != null && (Math.abs(x - downX) > halfSq || Math.abs(y - downY) > halfSq)) {
				isDragging = true;
			}

			if (isDragging) {
				dragPos.set(x, y);
				invalidate();
			}
			return true;
		case MotionEvent.ACTION_UP:
			if (isDragging && dragSq.isHighlighted()) {
				performClick();
			} else {
				var endSq = getTouchedSquare();
				if (endSq != null && endSq.getPiece() == Piece.EMPTY && endSq.isHighlighted()) {
					performClick();
				}
			}
			clearDrag();
			return true;
		case MotionEvent.ACTION_CANCEL:
			clearDrag();
			break;
		}
		return super.onTouchEvent(event);
	}

	private void clearDrag()
	{
		isDragging = false;
		dragSq = null;
		invalidate();
	}

	@Override
	public void onClick(View view)
	{
		IBoardSq sq = getTouchedSquare();
		if (sq != null && gameCtrl != null)
			gameCtrl.onBoardClick(sq);
	}

	@Override
	public boolean onLongClick(View view)
	{
		IBoardSq sq = getTouchedSquare();
		if (sq != null && gameCtrl != null) {
			gameCtrl.onBoardLongClick(sq);
			return true;
		}
		return false;
	}

	public IBoardSq getSquare(int index)
	{
		return squares[Board.EE64F(index)];
	}

	private IBoardSq getTouchedSquare()
	{
		int x = (int) Math.floor(lastTouch.getX() / sqSize);
		int y = (int) Math.floor(lastTouch.getY() / sqSize);
		int index = (8 * y + x);
		if (viewAsBlack)
			index = 63 - index;

		if (index < 0 || index >= 64)
			return null;
		return squares[index];
	}
}
