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

package com.chess.genesis.view;

import android.content.*;
import android.graphics.*;
import android.view.*;
import com.chess.genesis.engine.*;

public class PieceImg extends View
{
	protected final Matrix matrix = new Matrix();
	protected final PieceImgCache cache;

	protected int type = Piece.EMPTY;
	protected int count = 0;

	public PieceImg(final Context context, final PieceImgCache _cache)
	{
		super(context);
		cache = _cache;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(size, size);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		if (type == Piece.EMPTY)
			return;
		drawPiece(canvas);
		drawCount(canvas);
	}

	protected void drawPiece(final Canvas canvas)
	{
		canvas.drawBitmap(cache.getPieceImg(type + 6), matrix, null);
	}

	protected void drawCount(final Canvas canvas)
	{
		if (count == 1)
			return;
		canvas.drawBitmap(cache.getTokenImg(), matrix, null);
		canvas.drawBitmap(cache.getCountImg(count), matrix, null);
	}

	public void setPieceAndCount(final int Type, final int Count)
	{
		type = Type;
		count = Count;
		invalidate();
	}

	public int getPiece()
	{
		return type;
	}

	public void setPiece(final int Type)
	{
		type = Type;
		invalidate();
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
}
