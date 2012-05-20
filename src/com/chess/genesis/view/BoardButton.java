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
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;

public class BoardButton extends View
{
	private final static int outerLight = MColors.GREY_LIGHT;
	private final static int outerDark = MColors.BLUE_NAVY_DARK;
	private final static int innerDark = MColors.BLUE_NAVY;
	private final static int innerLight = Color.WHITE;
	private final static int innerCheck = MColors.RED_LIGHT;
	private final static int innerSelect = MColors.GREEN_TEAL;
	private final static int innerLast = MColors.PURPLE_LIGHT;

	private static final int WHITE = 0;
	private static final int BLACK = 1;

	private final Paint paint = new Paint();
	private final Matrix matrix = new Matrix();
	private final int squareColor;
	private final int squareIndex;

	private Bitmap localImg;
	private RectF inSquare;
	private int piece = 0;
	private int size;
	private boolean isHighlighted = false;
	private boolean isCheck = false;
	private boolean isLast = false;

	public BoardButton(final Context context, final int index)
	{
		super(context);

		squareIndex = index;
		squareColor = ((index / 16) % 2 == 1)?
				((index % 2 == 1)? BLACK : WHITE) :
				((index % 2 == 1)? WHITE : BLACK);
		setId(squareIndex);
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
		if (squareColor == WHITE)
			canvas.drawColor(outerLight);
		else
			canvas.drawColor(outerDark);

		// Draw inner square
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
		paint.setColor(innerColor);
		canvas.drawRect(inSquare, paint);

		// Draw piece image
		final Bitmap bitmap = (localImg == null)?
			BoardButtonCache.getPieceImg(piece + 6) :
			localImg;
		canvas.drawBitmap(bitmap, matrix, null);
	}

	public void resetSquare()
	{
		isHighlighted = false;
		isCheck = false;

		setPiece(Piece.EMPTY);
	}

	public void setImage(final Bitmap image)
	{
		localImg = image;
	}

	public void setPiece(final int piece_type)
	{
		piece = piece_type;
		invalidate();
	}

	public int getPiece()
	{
		return piece;
	}

	public int getIndex()
	{
		return squareIndex;
	}

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

final class BoardButtonCache extends PieceCache
{
	private static Bitmap[] scaledPieces;
	private static int size;
	private static boolean hasInit = false;

	private BoardButtonCache()
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
	}

	public static void resizeImages(final int newSize)
	{
		if (newSize == size || newSize < 1)
			return;

		size = newSize;
		for (int i = 0; i < 13; i++)
			scaledPieces[i] = createImg(i, size);
	}

	public static Bitmap getPieceImg(final int index)
	{
		return scaledPieces[index];
	}
}
