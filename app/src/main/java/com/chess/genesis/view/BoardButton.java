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
import com.chess.genesis.engine.*;

public class BoardButton extends PieceImg implements IBoardSq
{
	protected final static int WHITE = 0;
	protected final static int BLACK = 1;

	private final int squareColor;
	private final int squareIndex;

	private boolean isHighlighted = false;
	private boolean isCheck = false;
	private boolean isLast = false;

	public BoardButton(final Context context, final PieceImgCache _cache, final int index)
	{
		super(context, _cache, Piece.EMPTY);

		squareIndex = index;
		squareColor = ((index / 16) % 2 != 0)?
				((index % 2 != 0)? BLACK : WHITE) :
				((index % 2 != 0)? WHITE : BLACK);
		setId(squareIndex);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		final int outerColor = squareColor == WHITE? outerLight : outerDark;
		final int innerColor =
			isHighlighted?
				innerSelect :
			(isLast?
				innerLast :
			(isCheck?
				innerCheck :
			((squareColor == WHITE)?
				innerLight :
				innerDark)));
		drawSquare(canvas, innerColor, outerColor);
		drawPiece(canvas);
	}

	public void reset()
	{
		isHighlighted = false;
		isCheck = false;
		isLast = false;
		setPiece(Piece.EMPTY);
	}

	public int getIndex()
	{
		return squareIndex;
	}

	@Override
	public void setHighlight(final boolean mode)
	{
		isHighlighted = mode;
		invalidate();
	}

	public void setCheck(final boolean mode)
	{
		isCheck = mode;
		invalidate();
	}

	public void setLast(final boolean mode)
	{
		isLast = mode;
		invalidate();
	}
}
