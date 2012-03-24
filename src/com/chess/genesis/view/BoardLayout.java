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
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

class BoardLayout extends LinearLayout implements OnClickListener
{
	private GameState gamestate;

	public BoardLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		setOrientation(LinearLayout.VERTICAL);

		BoardButtonCache.Init(context);
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		size -= size % 8;

		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);

		// Update image caches
		BoardButtonCache.resizeImages(getMeasuredWidth() / 8);
		PlaceButtonCache.resizeImages(getMeasuredWidth() / 5);
	}

	public void init(final Context context, final GameState _gamestate, final boolean viewAsBlack)
	{
		gamestate = _gamestate;

		if (viewAsBlack) {
			for (int i = 0; i < 8; i++) {
				final ManualPanel row = new ManualPanel(context);
				row.setSizes("1,1,1,1,1,1,1,1/8");

				for (int j = 7; j >= 0; j--) {
					final BoardButton button = new BoardButton(context, 16 * i + j);
					button.setOnClickListener(this);
					row.addView(button);
				}
				addView(row);
			}
		} else {
			for (int i = 7; i >= 0; i--) {
				final ManualPanel row = new ManualPanel(context);
				row.setSizes("1,1,1,1,1,1,1,1/8");

				for (int j = 0; j < 8; j++) {
					final BoardButton button = new BoardButton(context, 16 * i + j);
					button.setOnClickListener(this);
					row.addView(button);
				}
				addView(row);
			}
		}
	}

	public void onClick(final View v)
	{
		gamestate.boardClick(v);
	}
}
