package com.chess.genesis;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

class PlaceButton extends FrameLayout
{
	private static final int[] pieceImages = {
		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
		R.drawable.square_none,
		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
		R.drawable.piece_white_queen,		R.drawable.piece_white_king};

	private static final int[] countImages = {
		R.drawable.piece_0,	R.drawable.piece_1,	R.drawable.piece_2,
		R.drawable.piece_3,	R.drawable.piece_4,	R.drawable.piece_5,
		R.drawable.piece_6,	R.drawable.piece_7,	R.drawable.piece_8,
		R.drawable.piece_9};

	private static final int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	private final int type;

	private int count;
	private boolean isHighlighted = false;

	public PlaceButton(final Context context, final int Type)
	{
		super(context);
		View.inflate(context, R.layout.framelayout_placebutton, this);
		setLayoutParams(PlaceLayout.LINEAR_PARAMS);

		type = Type;
		count = typeCounts[Math.abs(type)];
		setId(type + 100);

		setBoardImage(type);
		setPiece(type);
		setCountImage();
	}

	private void setBoardImage(final int value)
	{
		final int image = (value % 2 == 0)?
			R.drawable.square_light : R.drawable.square_dark;

		final MyImageView img = (MyImageView) findViewById(R.id.board_layer);
		img.setImageResource(image);
	}

	private void setPiece(final int type)
	{
		final MyImageView img = (MyImageView) findViewById(R.id.piece_layer);
		img.setImageResource(pieceImages[type + 6]);
	}

	private void setHighlightImage()
	{
		final int image = isHighlighted?
			R.drawable.square_ih_green :
			R.drawable.square_none;

		final MyImageView img = (MyImageView) findViewById(R.id.highlight_layer);
		img.setImageResource(image);
	}

	private void setCountImage()
	{
		final int image = countImages[count];

		final MyImageView img = (MyImageView) findViewById(R.id.count_layer);
		img.setImageResource(image);
	}

	public void reset()
	{
		isHighlighted = false;
		count = typeCounts[Math.abs(type)];

		setHighlightImage();
		setCountImage();
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
		setCountImage();
	}

	public void minusPiece()
	{
		count--;
		setCountImage();
	}
	
	public void plusPiece()
	{
		count++;
		setCountImage();
	}

	public void setHighlight(final boolean mode)
	{
		isHighlighted = mode;
		setHighlightImage();
	}
}
