package com.chess.genesis;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

abstract class PieceCache
{
	private static final int[] pieceImages = {
		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
		R.drawable.square_none,
		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
		R.drawable.piece_white_queen,		R.drawable.piece_white_king};

	protected static Bitmap[] pieceBitmaps;
	protected static Context cntx;

	private static boolean isActive = false;
	
	public static void InitPieces(final Context context)
	{
		if (isActive)
			return;
	try {
		pieceBitmaps = new Bitmap[13];

		cntx = context.createPackageContext(context.getPackageName(), Context.CONTEXT_INCLUDE_CODE);
		for (int i = 0; i < 13; i++)
			pieceBitmaps[i] = BitmapFactory.decodeResource(cntx.getResources(), pieceImages[i]);

	} catch (NameNotFoundException e) {
		throw new RuntimeException();
	}
		isActive = true;
	}
}
