/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
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
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;

public class PlaceButton extends PieceImgView implements IPlaceSq
{
	private final static int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	private int count;
	private boolean isHighlighted = false;
	private boolean drawZero;

	public PlaceButton(Context context, PieceImgPainter painter, int Type, boolean DrawZero)
	{
		super(context, painter, Type);
		count = typeCounts[Math.abs(type)];
		drawZero = DrawZero;
		setId(type + GameState.PLACEOFFSET);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		painter.drawSquare(canvas, this);
		painter.drawPiece(canvas, type);
		painter.drawCount(canvas, count, drawZero);
	}

	@Override
	public int getCount()
	{
		return count;
	}

	@Override
	public void setCount(final int Count)
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
	public void setHighlight(final boolean mode)
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
	public void reset()
	{
		isHighlighted = false;
		count = typeCounts[Math.abs(type)];
		invalidate();
	}
}
