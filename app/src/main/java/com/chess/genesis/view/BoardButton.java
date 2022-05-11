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
import android.view.*;
import com.chess.genesis.api.*;

public class BoardButton extends View implements IBoardSq
{
	private final BoardSquare square;

	public BoardButton(final Context context, final PieceImgPainter painter, final int index)
	{
		super(context);
		square = new BoardSquare(this, painter, index);
		setId(square.getIndex());
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
		square.draw(canvas);
	}

	@Override
	public void reset()
	{
		square.reset();
	}

	@Override
	public int getIndex()
	{
		return square.getIndex();
	}

	@Override
	public void setHighlight(final boolean mode)
	{
		square.setHighlight(mode);
	}

	@Override
	public boolean isHighlighted()
	{
		return square.isHighlighted();
	}

	@Override
	public void setCheck(final boolean mode)
	{
		square.setCheck(mode);
	}

	@Override
	public boolean isCheck()
	{
		return square.isCheck();
	}

	@Override
	public void setLast(final boolean mode)
	{
		square.setCheck(mode);
	}

	@Override
	public boolean isLast()
	{
		return square.isLast();
	}

	@Override
	public int getColor()
	{
		return square.getColor();
	}

	@Override
	public void setPiece(int piece)
	{
		square.setPiece(piece);
	}

	@Override
	public int getPiece()
	{
		return square.getPiece();
	}
}
