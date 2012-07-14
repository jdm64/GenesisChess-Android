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
import android.graphics.*;

public class PieceImgCache extends ImageCache
{
	public final static int PIECE_ONLY = 1;
	public final static int PIECE_COUNT = 3;

	private final Bitmap[] scaledPieces = new Bitmap[13];
	private final Bitmap[] scaledCount = new Bitmap[10];
	private Bitmap scaledToken;
	private int size;
	private final int type;

	public PieceImgCache(final Context context, final int Type)
	{
		setContext(context);
		type = Type;
	}

	public void resize(final int newSize)
	{
		if (newSize == size || newSize < 1)
			return;

		size = newSize;
		switch (type) {
		case PIECE_COUNT:
			scaledToken = createTokenImg(size);
			for (int i = 0; i < 10; i++)
				scaledCount[i] = createCountImg(i, size);
		case PIECE_ONLY:
			for (int i = 0; i < 13; i++)
				scaledPieces[i] = createPieceImg(i, size);
		}
	}

	public int getSize()
	{
		return size;
	}

	public Bitmap getPieceImg(final int index)
	{
		return scaledPieces[index];
	}

	public Bitmap getCountImg(final int index)
	{
		return scaledCount[index];
	}

	public Bitmap getTokenImg()
	{
		return scaledToken;
	}
}
