/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

class PlaceButton extends View
{
	private final static int outerLight = 0xffd3d3d3;
	private final static int outerDark = 0xff5a7397;
	private final static int innerDark = 0xff6885b4;
	private final static int innerLight = 0xffffffff;
	private final static int innerSelect = 0xff36b54a;

	private static final int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	private final Paint paint = new Paint();
	private final Matrix matrix = new Matrix();
	private final int type;

	private RectF inSquare;
	private int size;
	private int count;
	private boolean isHighlighted = false;

	public PlaceButton(final Context context, final int Type)
	{
		super(context);

		type = Type;
		count = typeCounts[Math.abs(type)];
		setId(type + 1000);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int newSize = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

		if (newSize != size) {
			size = newSize;
			inSquare = new RectF((float)(size * 0.09), (float)(size * 0.09), (float)(size * 0.91), (float)(size * 0.91));
		}
		setMeasuredDimension(size, size);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		// Draw outer square
		if (type % 2 == 0)
			canvas.drawColor(outerLight);
		else
			canvas.drawColor(outerDark);

		// Draw inner square
		final int innerColor =
			isHighlighted?
				innerSelect :
			((type % 2 == 0)?
				innerLight :
				innerDark);
		paint.setColor(innerColor);
		canvas.drawRect(inSquare, paint);

		// Draw piece image
		canvas.drawBitmap(PlaceButtonCache.getPieceImg(type + 6), matrix, null);

		// Draw token counter
		canvas.drawBitmap(PlaceButtonCache.getTokenImg(), matrix, null);
		canvas.drawBitmap(PlaceButtonCache.getCountImg(count), matrix, null);
	}

	public void reset()
	{
		isHighlighted = false;
		count = typeCounts[Math.abs(type)];

		invalidate();
	}

	public int getPiece()
	{
		return type;
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

	public void minusPiece()
	{
		count--;
		invalidate();
	}
	
	public void plusPiece()
	{
		count++;
		invalidate();
	}

	public void setHighlight(final boolean mode)
	{
		isHighlighted = mode;
		invalidate();
	}
}
