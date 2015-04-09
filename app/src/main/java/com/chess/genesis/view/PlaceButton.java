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

public class PlaceButton extends PieceImg
{
	private final static int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	private int count = 0;
	private boolean isHighlighted = false;

	public PlaceButton(final Context context, final PieceImgCache _cache, final int Type)
	{
		super(context, _cache, Type);
		count = typeCounts[Math.abs(type)];
		setId(type + 1000);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		final int outerColor = type % 2 == 0? outerLight : outerDark;
		final int innerColor =
			isHighlighted?
				innerSelect :
			((type % 2 == 0)?
				innerLight :
				innerDark);
		drawSquare(canvas, innerColor, outerColor);
		drawPiece(canvas);
		drawCount(canvas, count);
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(final int Count)
	{
		count = Count;
		invalidate();
	}

	public void minusCount()
	{
		count--;
		invalidate();
	}

	public void plusCount()
	{
		count++;
		invalidate();
	}

	@Override
	public void setHighlight(final boolean mode)
	{
		isHighlighted = mode;
		invalidate();
	}

	public void reset()
	{
		isHighlighted = false;
		count = typeCounts[Math.abs(type)];
		invalidate();
	}
}
