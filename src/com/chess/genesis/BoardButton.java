package com.chess.genesis;

import android.content.Context;

class BoardButton extends MyImageView
{
	private static final int[][] pieceImages = {
		{R.drawable.black_king_light,	R.drawable.black_king_dark},
		{R.drawable.black_queen_light,	R.drawable.black_queen_dark},
		{R.drawable.black_rook_light,	R.drawable.black_rook_dark},
		{R.drawable.black_bishop_light,	R.drawable.black_bishop_dark},
		{R.drawable.black_knight_light,	R.drawable.black_knight_dark},
		{R.drawable.black_pawn_light,	R.drawable.black_pawn_dark},
		{R.drawable.light_square,	R.drawable.dark_square},
		{R.drawable.white_pawn_light,	R.drawable.white_pawn_dark},
		{R.drawable.white_knight_light,	R.drawable.white_knight_dark},
		{R.drawable.white_bishop_light,	R.drawable.white_bishop_dark},
		{R.drawable.white_rook_light,	R.drawable.white_rook_dark},
		{R.drawable.white_queen_light,	R.drawable.white_queen_dark},
		{R.drawable.white_king_light,	R.drawable.white_king_dark} };

	private static final int[][] pieceImagesH = {
		{R.drawable.black_king_light_h,		R.drawable.black_king_dark_h},
		{R.drawable.black_queen_light_h,	R.drawable.black_queen_dark_h},
		{R.drawable.black_rook_light_h,		R.drawable.black_rook_dark_h},
		{R.drawable.black_bishop_light_h,	R.drawable.black_bishop_dark_h},
		{R.drawable.black_knight_light_h,	R.drawable.black_knight_dark_h},
		{R.drawable.black_pawn_light_h,		R.drawable.black_pawn_dark_h},
		{R.drawable.light_square,		R.drawable.dark_square},
		{R.drawable.white_pawn_light_h,		R.drawable.white_pawn_dark_h},
		{R.drawable.white_knight_light_h,	R.drawable.white_knight_dark_h},
		{R.drawable.white_bishop_light_h,	R.drawable.white_bishop_dark_h},
		{R.drawable.white_rook_light_h,		R.drawable.white_rook_dark_h},
		{R.drawable.white_queen_light_h,	R.drawable.white_queen_dark_h},
		{R.drawable.white_king_light_h,		R.drawable.white_king_dark_h} };

	private static final int[][] pieceImagesL = {
		{R.drawable.black_king_light_l,		R.drawable.black_king_dark_l},
		{R.drawable.black_queen_light_l,	R.drawable.black_queen_dark_l},
		{R.drawable.black_rook_light_l,		R.drawable.black_rook_dark_l},
		{R.drawable.black_bishop_light_l,	R.drawable.black_bishop_dark_l},
		{R.drawable.black_knight_light_l,	R.drawable.black_knight_dark_l},
		{R.drawable.black_pawn_light_l,		R.drawable.black_pawn_dark_l},
		{R.drawable.light_square,		R.drawable.dark_square},
		{R.drawable.white_pawn_light_l,		R.drawable.white_pawn_dark_l},
		{R.drawable.white_knight_light_l,	R.drawable.white_knight_dark_l},
		{R.drawable.white_bishop_light_l,	R.drawable.white_bishop_dark_l},
		{R.drawable.white_rook_light_l,		R.drawable.white_rook_dark_l},
		{R.drawable.white_queen_light_l,	R.drawable.white_queen_dark_l},
		{R.drawable.white_king_light_l,		R.drawable.white_king_dark_l} };

	private static final int[][] kingImages = {
		{R.drawable.black_king_light_c,		R.drawable.black_king_dark_c},
		{R.drawable.white_king_light_c,		R.drawable.white_king_dark_c} };

	private static final int WHITE = 0;
	private static final int BLACK = 1;

	private final int squareColor;
	private final int squareIndex;

	private int piece = 0;
	private boolean isHighlighted = false;
	private boolean isCheck = false;
	private boolean isLast = false;

	public BoardButton(final Context context, final int index)
	{
		super(context);

		squareIndex = index;
		squareColor = ((index / 8) % 2 == 1)? 
				((index % 2 == 1)? WHITE : BLACK) :
				((index % 2 == 1)? BLACK : WHITE);

		setId(squareIndex);
		setSquareImage();
	}

	private void setSquareImage()
	{
		final int image = isHighlighted?
			pieceImagesH[piece + 6][squareColor] :
			(isLast?
				pieceImagesL[piece + 6][squareColor] :
			(isCheck?
				kingImages[(piece > 0)? 1:0][squareColor] :
				pieceImages[piece + 6][squareColor]));

		setImageResource(image);
	}

	public void resetSquare()
	{
		piece = 0;
		isHighlighted = false;
		isCheck = false;

		setSquareImage();
	}

	public void setPiece(final int Piece)
	{
		piece = Piece;

		setSquareImage();
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

		setSquareImage();
	}

	public void setCheck(final boolean mode)
	{
		isCheck = mode;

		setSquareImage();
	}

	public void setLast(final boolean mode)
	{
		isLast = mode;

		setSquareImage();
	}
}
