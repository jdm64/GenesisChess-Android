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
import android.preference.*;
import android.util.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;

public class CapturedLayout extends ManualPanel
{
	private final PieceImg[] pieces = new PieceImg[11];
	private final PieceImgCache cache;
	private final boolean isEnabled;

	public CapturedLayout(final Context context, final AttributeSet attributeSet)
	{
		super(context, attributeSet);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		isEnabled = pref.getBoolean(PrefKey.SHOW_CAPTURED, true);

		if (!isEnabled) {
			cache = null;
			return;
		}
		cache = new PieceImgCache(context, PieceImgCache.PIECE_COUNT);
		initPieces();
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!isEnabled)
			return;
		final int size = Math.min(getMeasuredHeight(), getMeasuredWidth() / 11);
		cache.resize(size);
	}

	private void initPieces()
	{
		for (int i = 0; i < pieces.length; i++) {
			pieces[i] = new PieceImg(getContext(), cache);
			addView(pieces[i]);
		}
	}

	public void setPieces(final int[] counts)
	{
		if (!isEnabled)
			return;

		// Black pieces {King, ..., Pawn}
		int j = 0;
		for (int i = 0; i < 6; i++) {
			if (counts[i] > 0)
				pieces[j++].setPieceAndCount(i - 6, counts[i]);
		}
		// set the rest of the pieces to no piece
		while (j < 5)
			pieces[j++].setPieceAndCount(Piece.EMPTY, 0);

		// White pieces {Pawn, ..., King}
		j = 10;
		for (int i = 12; i > 6; i--) {
			if (counts[i] > 0)
				pieces[j--].setPieceAndCount(i - 6, counts[i]);
		}
		// set the rest of the pieces to no piece
		while (j > 5)
			pieces[j--].setPieceAndCount(Piece.EMPTY, 0);
	}
}
