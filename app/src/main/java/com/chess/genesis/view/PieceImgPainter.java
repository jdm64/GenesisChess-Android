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
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import com.chess.genesis.*;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;

public final class PieceImgPainter
{
	final static int WHITE = 0;
	final static int BLACK = 1;

	private static int outerLight;
	private static int outerDark;
	private static int innerDark;
	private static int innerLight;
	private static int innerSelect;
	private static int innerCheck;
	private static int innerLast;
	private static boolean colorsSet = false;

	private final static float recScale = (float) 0.9;

	private final PieceImgCache cache;
	private final Paint paint = new Paint();
	private final RectF inSquare = new RectF();
	private final RectF outSquare = new RectF();

	private int x;
	private int y;

	public PieceImgPainter(final Context context)
	{
		cache = new PieceImgCache(context);
		initColors(context);
	}

	private static void initColors(final Context context)
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
		new PrefEdit(context)
			.putInt(R.array.pf_bcInnerDark)
			.putInt(R.array.pf_bcInnerLight)
			.putInt(R.array.pf_bcOuterDark)
			.putInt(R.array.pf_bcOuterLight)
			.putInt(R.array.pf_bcInnerSelect)
			.putInt(R.array.pf_bcInnerCheck)
			.putInt(R.array.pf_bcInnerLast)
			.commit();

		setColors(context);
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
		int index = sq.getIndex();
		int outerColor = index % 2 == 0? outerLight : outerDark;
		int innerColor =
			sq.isHighlighted()?
				innerSelect :
			((index % 2 == 0)?
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
		canvas.drawBitmap(cache.getPieceImg(type + 6), x, y, null);
	}

	public void drawCount(final Canvas canvas, final int count, final boolean drawZero)
	{
		if (count <= 1 && !drawZero)
			return;
		canvas.drawBitmap(cache.getTokenImg(), x, y, null);
		canvas.drawBitmap(cache.getCountImg(count), x, y, null);
	}

	public int getSize()
	{
		return cache.getSize();
	}

	public void resize(int newSize)
	{
		int size = cache.getSize();
		if (size == newSize || newSize < 1)
			return;

		size = newSize;
		final float rb = size * recScale, lt = size - rb;
		inSquare.set(lt, lt, rb, rb);
		outSquare.set(0, 0, size, size);
		cache.resize(size);
	}

	public void offsetTo(int X, int Y)
	{
		x = X;
		y = Y;

		float d = (1 - recScale) * cache.getSize();
		inSquare.offsetTo(x, y);
		inSquare.offset(d, d);
		outSquare.offsetTo(x, y);
	}
}

final class PieceImgCache
{
	private static final int[] pieceRes = {
		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
		R.drawable.square_none,
		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
		R.drawable.piece_white_queen,		R.drawable.piece_white_king};

	private static final int[] countRes = {
		R.drawable.piece_0,	R.drawable.piece_1,	R.drawable.piece_2,
		R.drawable.piece_3,	R.drawable.piece_4,	R.drawable.piece_5,
		R.drawable.piece_6,	R.drawable.piece_7,	R.drawable.piece_8,
		R.drawable.piece_9};

	private static final int tokenRes = R.drawable.piece_token;

	private static Resources resource;

	private final Bitmap[] pieceImg = new Bitmap[13];
	private final Bitmap[] countImg = new Bitmap[10];
	private Bitmap tokenImg;
	private int size;

	public PieceImgCache(final Context context)
	{
		try {
			final Context cntx = context.createPackageContext(context.getPackageName(), Context.CONTEXT_INCLUDE_CODE);
			resource = cntx.getResources();
		} catch (final PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static Bitmap loadImage(final int id, final int isize)
	{
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resource, id), isize, isize, true);
	}

	public void resize(final int newSize)
	{
		if (newSize == size || newSize < 1)
			return;
		size = newSize;
	}

	public int getSize()
	{
		return size;
	}

	public Bitmap getPieceImg(final int index)
	{
		if (pieceImg[index] == null)
			pieceImg[index] = loadImage(pieceRes[index], size);
		return pieceImg[index];
	}

	public Bitmap getCountImg(final int index)
	{
		if (countImg[index] == null)
			countImg[index] = loadImage(countRes[index], size);
		return countImg[index];
	}

	public Bitmap getTokenImg()
	{
		if (tokenImg == null)
			tokenImg = loadImage(tokenRes, size);
		return tokenImg;
	}
}
