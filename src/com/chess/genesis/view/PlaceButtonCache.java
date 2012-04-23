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

package com.chess.genesis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
