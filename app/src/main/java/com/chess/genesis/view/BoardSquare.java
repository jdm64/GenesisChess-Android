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

import android.graphics.*;
import android.view.*;

import com.chess.genesis.engine.*;

public class BoardSquare implements IBoardSq
{
	private final View view;
	private final PieceImgPainter painter;
	private final int color;
	private final int index;

	private int x;
	private int y;

	private int type = Piece.EMPTY;
	private boolean isHighlighted = false;
	private boolean isCheck = false;
	private boolean isLast = false;

	public BoardSquare(View _view, PieceImgPainter _painter, int _index)
	{
		view = _view;
		painter = _painter;
		index = _index;
		color = ((index / 16) % 2 != 0)?
			((index % 2 != 0)? PieceImgPainter.BLACK : PieceImgPainter.WHITE) :
			((index % 2 != 0)? PieceImgPainter.WHITE : PieceImgPainter.BLACK);
	}

	public void draw(final Canvas canvas)
	{
		painter.offsetTo(x, y);
		painter.drawSquare(canvas, this);
		painter.drawPiece(canvas, type);
	}

	@Override
	public void setPiece(int piece)
	{
		type = piece;
		view.invalidate();
	}

	@Override
	public int getPiece()
	{
		return type;
	}

	@Override
	public void reset()
	{
		isHighlighted = false;
		isCheck = false;
		isLast = false;
		type = Piece.EMPTY;
		view.invalidate();
	}

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public void setHighlight(final boolean mode)
	{
		isHighlighted = mode;
		view.invalidate();
	}

	@Override
	public boolean isHighlighted()
	{
		return isHighlighted;
	}

	@Override
	public void setCheck(final boolean mode)
	{
		isCheck = mode;
		view.invalidate();
	}

	@Override
	public boolean isCheck()
	{
		return isCheck;
	}

	@Override
	public void setLast(final boolean mode)
	{
		isLast = mode;
		view.invalidate();
	}

	@Override
	public boolean isLast()
	{
		return isLast;
	}

	@Override
	public int getColor()
	{
		return color;
	}

	public void setXY(int X, int Y)
	{
		x = X;
		y = Y;
	}
}
