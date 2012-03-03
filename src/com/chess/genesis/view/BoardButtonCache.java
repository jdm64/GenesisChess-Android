package com.chess.genesis;

import android.content.Context;
import android.graphics.Bitmap;

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
			scaledPieces[i] = Bitmap.createScaledBitmap(pieceBitmaps[i], size, size, true);
	}

	public static Bitmap getPieceImg(final int index)
	{
		return scaledPieces[index];
	}
}
