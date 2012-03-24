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
