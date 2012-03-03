package com.chess.genesis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

class BoardButton extends View
{
	private final static int outerLight = 0xffd3d3d3;
	private final static int outerDark = 0xff5a7397;
	private final static int innerDark = 0xff6885b4;
	private final static int innerLight = 0xffffffff;
	private final static int innerCheck = 0xffdf4c37;
	private final static int innerSelect = 0xff36b54a;
	private final static int innerLast = 0xffab9aca;

	private static final int WHITE = 0;
	private static final int BLACK = 1;

	private final Paint paint = new Paint();
	private final Matrix matrix = new Matrix();
	private final int squareColor;
	private final int squareIndex;

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
		canvas.drawBitmap(BoardButtonCache.getPieceImg(piece + 6), matrix, null);
	}

	public void resetSquare()
	{
		isHighlighted = false;
		isCheck = false;

		setPiece(Piece.EMPTY);
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
