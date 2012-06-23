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
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class PlaceButton extends View
{
	private final static int outerLight = MColors.GREY_LIGHT;
	private final static int outerDark = MColors.BLUE_NAVY_DARK;
	private final static int innerDark = MColors.BLUE_NAVY;
	private final static int innerLight = Color.WHITE;
	private final static int innerSelect = MColors.GREEN_TEAL;

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

final class PlaceButtonCache extends PieceCache
{
	private static final int[] countImages = {
		R.drawable.piece_0,	R.drawable.piece_1,	R.drawable.piece_2,
		R.drawable.piece_3,	R.drawable.piece_4,	R.drawable.piece_5,
		R.drawable.piece_6,	R.drawable.piece_7,	R.drawable.piece_8,
		R.drawable.piece_9};

	private static Bitmap[] scaledCounts;
	private static Bitmap[] scaledPieces;
	private static Bitmap scaledToken;

	private static int size;
	private static boolean hasInit = false;

	private PlaceButtonCache()
	{
	}

	public static void Init(final Context context)
	{
		InitPieces(context);

		if (hasInit)
			return;

		size = 0;
		hasInit = true;
		scaledPieces = new Bitmap[13];
		scaledCounts = new Bitmap[10];
	}

	public static void resizeImages(final int newSize)
	{
		if (newSize == size || newSize < 1)
			return;

		size = newSize;
		for (int i = 0; i < 13; i++)
			scaledPieces[i] = createImg(i, size);

		final Bitmap[] countBitmaps = new Bitmap[10];
		for (int i = 0; i < 10; i++) {
			countBitmaps[i] = BitmapFactory.decodeResource(cntx.getResources(), countImages[i]);
			scaledCounts[i] = Bitmap.createScaledBitmap(countBitmaps[i], size, size, true);
		}
		final Bitmap tokenBitmap = BitmapFactory.decodeResource(cntx.getResources(), R.drawable.piece_token);
		scaledToken = Bitmap.createScaledBitmap(tokenBitmap, size, size, true);
	}

	public static Bitmap getPieceImg(final int index)
	{
		return scaledPieces[index];
	}

	public static Bitmap getCountImg(final int count)
	{
		return scaledCounts[count];
	}

	public static Bitmap getTokenImg()
	{
		return scaledToken;
	}
}
