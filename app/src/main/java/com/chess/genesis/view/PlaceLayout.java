/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.view;

import android.content.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;

public class PlaceLayout extends LinearLayout implements OnClickListener
{
	public static final LinearLayout.LayoutParams LINEAR_PARAMS =
		new LinearLayout.LayoutParams(
			android.view.ViewGroup.LayoutParams.MATCH_PARENT,
			android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

	private final static int[] piecelist = {1, 2, 3, 4, 5, 6, -1, -2, -3, -4, -5, -6};

	private GameState gamestate;
	private final PieceImgCache cache;

	public PlaceLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		cache = new PieceImgCache(context);
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		size -= size % 5;

		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);
		cache.resize(getMeasuredHeight() / 5);
	}

	private void AddPieces(final Context context, final int index)
	{
		for (int i = 0, idx = index; i < 2; i++) {
			final ManualPanel row = new ManualPanel(context);
			row.setSizes("1/5");

			row.addView(new BoardButton(context, cache, i + 10000));

			for (int j = 0; j < 3; j++) {
				final PlaceButton button = new PlaceButton(context, cache, piecelist[idx++]);
				button.setOnClickListener(this);
				row.addView(button);
			}
			row.addView(new BoardButton(context, cache, i + 10000));
			addView(row);
		}
	}

	public void init(final GameState _gamestate)
	{
		gamestate = _gamestate;
		final Context context = getContext();

		// White Pieces
		AddPieces(context, 0);

		// Center Divide
		final LinearLayout row = new LinearLayout(context);
		final MyImageView padding = new MyImageView(context);
		padding.setResId(R.drawable.padding_480x96);
		row.addView(padding);
		addView(row);

		// Black Pieces
		AddPieces(context, 6);
	}

	@Override
	public void onClick(final View v)
	{
		gamestate.placeClick(v);
	}
}