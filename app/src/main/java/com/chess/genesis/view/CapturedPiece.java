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

public class CapturedPiece extends PieceImg
{
	private int count = 0;

	public CapturedPiece(final Context context, final PieceImgCache _cache)
	{
		super(context, _cache, Piece.NONE);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		drawPiece(canvas);
		if (count > 1)
			drawCount(canvas, count);
	}

	@Override
	public void setHighlight(final boolean mode)
	{
		// do nothing
	}

	public void setPieceAndCount(final int piece, final int Count)
	{
		count = Count;
		setPiece(piece);
	}
}
