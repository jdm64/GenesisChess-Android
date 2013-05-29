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
import android.content.res.*;
import android.graphics.*;
import com.chess.genesis.*;

public class PieceImgCache
{
	private static final int[] pieceRes = {
		R.drawable.piece_black_king,		R.drawable.piece_black_queen,
		R.drawable.piece_black_rook,		R.drawable.piece_black_bishop,
		R.drawable.piece_black_knight,		R.drawable.piece_black_pawn,
		R.drawable.square_none,
		R.drawable.piece_white_pawn,		R.drawable.piece_white_knight,
		R.drawable.piece_white_bishop,		R.drawable.piece_white_rook,
		R.drawable.piece_white_queen,		R.drawable.piece_white_king};

	private static final int[] countRes = {
		R.drawable.piece_0,	R.drawable.piece_1,	R.drawable.piece_2,
		R.drawable.piece_3,	R.drawable.piece_4,	R.drawable.piece_5,
		R.drawable.piece_6,	R.drawable.piece_7,	R.drawable.piece_8,
		R.drawable.piece_9};

	private static final int tokenRes = R.drawable.piece_token;

	private static Resources resource;

	private final Bitmap[] pieceImg = new Bitmap[13];
	private final Bitmap[] countImg = new Bitmap[10];
	private Bitmap tokenImg;
	private int size;

	public PieceImgCache(final Context context)
	{
	try {
		final Context cntx = context.createPackageContext(context.getPackageName(), Context.CONTEXT_INCLUDE_CODE);
		resource = cntx.getResources();
	} catch (final NameNotFoundException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private static Bitmap loadImage(final int id, final int isize)
	{
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resource, id), isize, isize, true);
	}

	public void resize(final int newSize)
	{
		if (newSize == size || newSize < 1)
			return;
		size = newSize;
	}

	public int getSize()
	{
		return size;
	}

	public Bitmap getPieceImg(final int index)
	{
		if (pieceImg[index] == null)
			pieceImg[index] = loadImage(pieceRes[index], size);
		return pieceImg[index];
	}

	public Bitmap getCountImg(final int index)
	{
		if (countImg[index] == null)
			countImg[index] = loadImage(countRes[index], size);
		return countImg[index];
	}

	public Bitmap getTokenImg()
	{
		if (tokenImg == null)
			tokenImg = loadImage(tokenRes, size);
		return tokenImg;
	}
}
