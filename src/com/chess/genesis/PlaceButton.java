package com.chess.genesis;

import android.content.Context;

class PlaceButton extends MyImageView
{
	private static final int[][] pieceImages = {
		{R.drawable.black_king_0,	R.drawable.black_king_1},
		{R.drawable.black_queen_0,	R.drawable.black_queen_1,	R.drawable.black_queen_2,
		R.drawable.black_queen_3,	R.drawable.black_queen_4,	R.drawable.black_queen_5,
		R.drawable.black_queen_6,	R.drawable.black_queen_7,	R.drawable.black_queen_8,
		R.drawable.black_queen_9},
		{R.drawable.black_rook_0,	R.drawable.black_rook_1,	R.drawable.black_rook_2},
		{R.drawable.black_bishop_0,	R.drawable.black_bishop_1,	R.drawable.black_bishop_2},
		{R.drawable.black_knight_0,	R.drawable.black_knight_1,	R.drawable.black_knight_2},
		{R.drawable.black_pawn_0,	R.drawable.black_pawn_1,	R.drawable.black_pawn_2,
		R.drawable.black_pawn_3,	R.drawable.black_pawn_4,	R.drawable.black_pawn_5,
		R.drawable.black_pawn_6,	R.drawable.black_pawn_7,	R.drawable.black_pawn_8},
		{0},
		{R.drawable.white_pawn_0,	R.drawable.white_pawn_1,	R.drawable.white_pawn_2,
		R.drawable.white_pawn_3,	R.drawable.white_pawn_4,	R.drawable.white_pawn_5,
		R.drawable.white_pawn_6,	R.drawable.white_pawn_7,	R.drawable.white_pawn_8},
		{R.drawable.white_knight_0,	R.drawable.white_knight_1,	R.drawable.white_knight_2},
		{R.drawable.white_bishop_0,	R.drawable.white_bishop_1,	R.drawable.white_bishop_2},
		{R.drawable.white_rook_0,	R.drawable.white_rook_1,	R.drawable.white_rook_2},
		{R.drawable.white_queen_0,	R.drawable.white_queen_1,	R.drawable.white_queen_2,
		R.drawable.white_queen_3,	R.drawable.white_queen_4,	R.drawable.white_queen_5,
		R.drawable.white_queen_6,	R.drawable.white_queen_7,	R.drawable.white_queen_8,
		R.drawable.white_queen_9},
		{R.drawable.white_king_0,	R.drawable.white_king_1} };

	private static final int[][] pieceImagesH = {
		{R.drawable.black_king_0h,	R.drawable.black_king_1h},
		{R.drawable.black_queen_0h,	R.drawable.black_queen_1h},
		{R.drawable.black_rook_0h,	R.drawable.black_rook_1h,	R.drawable.black_rook_2h},
		{R.drawable.black_bishop_0h,	R.drawable.black_bishop_1h,	R.drawable.black_bishop_2h},
		{R.drawable.black_knight_0h,	R.drawable.black_knight_1h,	R.drawable.black_knight_2h},
		{R.drawable.black_pawn_0h,	R.drawable.black_pawn_1h,	R.drawable.black_pawn_2h,
		R.drawable.black_pawn_3h,	R.drawable.black_pawn_4h,	R.drawable.black_pawn_5h,
		R.drawable.black_pawn_6h,	R.drawable.black_pawn_7h,	R.drawable.black_pawn_8h},
		{0},
		{R.drawable.white_pawn_0h,	R.drawable.white_pawn_1h,	R.drawable.white_pawn_2h,
		R.drawable.white_pawn_3h,	R.drawable.white_pawn_4h,	R.drawable.white_pawn_5h,
		R.drawable.white_pawn_6h,	R.drawable.white_pawn_7h,	R.drawable.white_pawn_8h},
		{R.drawable.white_knight_0h,	R.drawable.white_knight_1h,	R.drawable.white_knight_2h},
		{R.drawable.white_bishop_0h,	R.drawable.white_bishop_1h,	R.drawable.white_bishop_2h},
		{R.drawable.white_rook_0h,	R.drawable.white_rook_1h,	R.drawable.white_rook_2h},
		{R.drawable.white_queen_0h,	R.drawable.white_queen_1h},
		{R.drawable.white_king_0h,	R.drawable.white_king_1h} };

	private static final int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	private final int type;

	private int count;
	private boolean isHighlighted = false;

	public PlaceButton(final Context context, final int Type)
	{
		super(context);

		type = Type;
		count = typeCounts[Math.abs(type)];

		setId(type + 100);
		setPieceImage();
		setLayoutParams(PlaceLayout.LINEAR_PARAMS);
	}

	private void setPieceImage()
	{
		final int image = (isHighlighted)?
			pieceImagesH[type + 6][count] :
			pieceImages[type + 6][count];

		setImageResource(image);
	}
	
	public void reset()
	{
		isHighlighted = false;
		count = typeCounts[Math.abs(type)];

		setPieceImage();
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

		setPieceImage();
	}

	public void minusPiece()
	{
		count--;

		setPieceImage();
	}
	
	public void plusPiece()
	{
		count++;

		setPieceImage();
	}

	public void setHighlight(final boolean mode)
	{
		isHighlighted = mode;

		setPieceImage();
	}
}
