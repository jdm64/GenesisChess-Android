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
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.*;
import com.chess.genesis.*;

public abstract class PieceCache
{
	private static final int[] pieceImages = {
		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
		R.drawable.square_none,
		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
		R.drawable.piece_white_queen,		R.drawable.piece_white_king};

	private static Bitmap[] pieceBitmaps;
	private static boolean isActive = false;

	protected static Context cntx;

	public static void InitPieces(final Context context)
	{
		if (isActive)
			return;
	try {
		pieceBitmaps = new Bitmap[13];

		cntx = context.createPackageContext(context.getPackageName(), Context.CONTEXT_INCLUDE_CODE);
		for (int i = 0; i < 13; i++)
			pieceBitmaps[i] = BitmapFactory.decodeResource(cntx.getResources(), pieceImages[i]);

	} catch (final NameNotFoundException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
		isActive = true;
	}

	public static Bitmap createImg(final int type, final int size)
	{
		return Bitmap.createScaledBitmap(pieceBitmaps[type], size, size, true);
	}
}
