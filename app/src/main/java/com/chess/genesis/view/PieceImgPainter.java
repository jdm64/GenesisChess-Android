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
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;

public final class PieceImgPainter
{
	protected final static int WHITE = 0;
	protected final static int BLACK = 1;

	private static int outerLight;
	private static int outerDark;
	private static int innerDark;
	private static int innerLight;
	private static int innerSelect;
	private static int innerCheck;
	private static int innerLast;
	private static boolean colorsSet = false;

	private final static Matrix matrix = new Matrix();
	private final static float recScale = (float) 0.9;

	private final PieceImgCache cache;
	private final Paint paint = new Paint();
	private final RectF inSquare = new RectF();
	private final RectF outSquare = new RectF();

	public PieceImgPainter(final Context context)
	{
		cache = new PieceImgCache(context);
	}

	public static void initColors(final Context context)
	{
		if (colorsSet)
			return;
		setColors(context);
	}

	public static void setColors(final Context context)
	{
		final Pref pref = new Pref(context);
		outerLight = pref.getInt(R.array.pf_bcOuterLight);
		outerDark = pref.getInt(R.array.pf_bcOuterDark);
		innerDark = pref.getInt(R.array.pf_bcInnerDark);
		innerLight = pref.getInt(R.array.pf_bcInnerLight);
		innerSelect = pref.getInt(R.array.pf_bcInnerSelect);
		innerCheck = pref.getInt(R.array.pf_bcInnerCheck);
		innerLast = pref.getInt(R.array.pf_bcInnerLast);
		colorsSet = true;
	}

	public static void resetColors(final Context context)
	{
		final PrefEdit pref = new PrefEdit(context);
		pref.putInt(R.array.pf_bcInnerDark);
		pref.putInt(R.array.pf_bcInnerLight);
		pref.putInt(R.array.pf_bcOuterDark);
		pref.putInt(R.array.pf_bcOuterLight);
		pref.putInt(R.array.pf_bcInnerSelect);
		pref.putInt(R.array.pf_bcInnerCheck);
		pref.putInt(R.array.pf_bcInnerLast);
		pref.commit();
		colorsSet = false;
	}

	public void drawSquare(final Canvas canvas, IBoardSq sq)
	{
		boolean isWhite = sq.getColor() == WHITE;
		int outerColor = isWhite? outerLight : outerDark;
		int innerColor =
			sq.isHighlighted()?
				innerSelect :
			(sq.isLast()?
				innerLast :
			(sq.isCheck()?
				innerCheck :
			(isWhite?
				innerLight :
				innerDark)));
		drawSquare(canvas, innerColor, outerColor);
	}

	public void drawSquare(final Canvas canvas, IPlaceSq sq)
	{
		int piece = sq.getPiece();
		int outerColor = piece % 2 == 0? outerLight : outerDark;
		int innerColor =
			sq.isHighlighted()?
				innerSelect :
			((piece % 2 == 0)?
				innerLight :
				innerDark);
		drawSquare(canvas, innerColor, outerColor);
	}

	private void drawSquare(final Canvas canvas, final int inColor, final int outColor)
	{
		paint.setColor(outColor);
		canvas.drawRect(outSquare, paint);
		paint.setColor(inColor);
		canvas.drawRect(inSquare, paint);
	}

	public void drawPiece(final Canvas canvas, final int type)
	{
		canvas.drawBitmap(cache.getPieceImg(type + 6), matrix, null);
	}

	public void drawCount(final Canvas canvas, final int count, final boolean drawZero)
	{
		if (count <= 1 && !drawZero)
			return;
		canvas.drawBitmap(cache.getTokenImg(), matrix, null);
		canvas.drawBitmap(cache.getCountImg(count), matrix, null);
	}

	public void resize(int newSize)
	{
		int size = cache.getSize();
		if (size == newSize || newSize < 1)
			return;

		final float rb = size * recScale, lt = size - rb;
		inSquare.set(lt, lt, rb, rb);
		outSquare.set(0, 0, size, size);
	}
}
