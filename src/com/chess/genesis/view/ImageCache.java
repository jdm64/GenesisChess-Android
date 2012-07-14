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

public abstract class ImageCache
{
	private static final int[] pieceRes = {
		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
		R.drawable.square_none,
		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
		R.drawable.piece_white_queen,		R.drawable.piece_white_king};
	private static Bitmap[] pieceBitmaps = new Bitmap[pieceRes.length];

	private static final int[] countRes = {
		R.drawable.piece_0,	R.drawable.piece_1,	R.drawable.piece_2,
		R.drawable.piece_3,	R.drawable.piece_4,	R.drawable.piece_5,
		R.drawable.piece_6,	R.drawable.piece_7,	R.drawable.piece_8,
		R.drawable.piece_9};
	private static Bitmap[] countBitmaps = new Bitmap[countRes.length];

	private static final int tokenRes = R.drawable.piece_token;
	private static Bitmap tokenBitmap;

	private static Context cntx;

	protected static void setContext(final Context context)
	{
	try {
		cntx = context.createPackageContext(context.getPackageName(), Context.CONTEXT_INCLUDE_CODE);
	} catch (final NameNotFoundException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	protected static Bitmap createPieceImg(final int type, final int size)
	{
		if (pieceBitmaps[type] == null)
			pieceBitmaps[type] = BitmapFactory.decodeResource(cntx.getResources(), pieceRes[type]);
		return Bitmap.createScaledBitmap(pieceBitmaps[type], size, size, true);
	}

	protected static Bitmap createCountImg(final int count, final int size)
	{
		if (countBitmaps[count] == null)
			countBitmaps[count] = BitmapFactory.decodeResource(cntx.getResources(), countRes[count]);
		return Bitmap.createScaledBitmap(countBitmaps[count], size, size, true);
	}

	protected static Bitmap createTokenImg(final int size)
	{
		if (tokenBitmap == null)
			tokenBitmap = BitmapFactory.decodeResource(cntx.getResources(), tokenRes);
		return Bitmap.createScaledBitmap(tokenBitmap, size, size, true);
	}
}
