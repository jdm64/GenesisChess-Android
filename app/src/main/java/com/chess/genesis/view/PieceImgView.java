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
import android.view.*;
import com.chess.genesis.api.*;

public class PieceImgView extends View implements ICountSq
{
	final PieceImgPainter painter;
	final int initCount;
	final boolean drawZero;
	final boolean drawBoard;

	int type;
	int count;
	boolean isHighlighted = false;

	public PieceImgView(Context context, PieceImgPainter Painter, int index, int Type, int Count, boolean DrawZero, boolean DrawBoard)
	{
		super(context);
		painter = Painter;
		type = Type;
		initCount = Count;
		count = Count;
		drawZero = DrawZero;
		drawBoard = DrawBoard;
		setId(index);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		var size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(size, size);
		painter.resize(size);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (drawBoard) {
			painter.drawSquare(canvas, this);
		}
		painter.drawHighlight(canvas, this);

		if (count != 0) {
			painter.drawPiece(canvas, type);
		}
		painter.drawCount(canvas, count, drawZero);
	}

	@Override
	public void setPiece(int Type)
	{
		type = Type;
		invalidate();
	}

	@Override
	public int getPiece()
	{
		return type;
	}

	@Override
	public int getCount()
	{
		return count;
	}

	@Override
	public void setCount(int Count)
	{
		count = Count;
		invalidate();
	}

	@Override
	public void minusCount()
	{
		count--;
		invalidate();
	}

	@Override
	public void plusCount()
	{
		count++;
		invalidate();
	}

	@Override
	public int getIndex()
	{
		return getId();
	}

	@Override
	public void setHighlight(boolean mode)
	{
		isHighlighted = mode;
		invalidate();
	}

	@Override
	public boolean isHighlighted()
	{
		return isHighlighted;
	}

	@Override
	public void setPieceAndCount(int piece, int Count)
	{
		count = Count;
		setPiece(piece);
	}

	@Override
	public void reset()
	{
		isHighlighted = false;
		count = initCount;
		invalidate();
	}
}
