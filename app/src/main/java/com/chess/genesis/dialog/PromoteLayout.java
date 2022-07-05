/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.dialog;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;

public class PromoteLayout extends LinearLayout implements View.OnClickListener, View.OnTouchListener
{
	private final DisplayMetrics METRICS = new DisplayMetrics();
	private final BoardButton[] square = new BoardButton[4];
	private final PieceImgPainter painter;
	private PawnPromoteDialog dialog;

	public PromoteLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		painter = new PieceImgPainter(context);
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		Util.getMetrics(METRICS, getContext());

		int size = 2 * Math.min(METRICS.heightPixels, METRICS.widthPixels) / 5;
		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);
		painter.resize(getMeasuredWidth() / 2);
	}

	public void init(final Context context, final PawnPromoteDialog _dialog, final int color)
	{
		dialog = _dialog;

		for (int i = 0, piece = Piece.QUEEN; i < 4;) {
			final ManualPanel row = new ManualPanel(context);
			row.setSizes("1/2");

			for (int j = 0; j < 2; piece--, j++, i++) {
				square[i] = new BoardButton(context, painter, (i < 2)? j : j + 1);
				square[i].setOnClickListener(this);
				square[i].setOnTouchListener(this);
				square[i].setPiece(piece * color);
				row.addView(square[i]);
			}
			addView(row);
		}
	}

	@Override
	public void onClick(final View v)
	{
		dialog.onClick(v);
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event)
	{
		((IBoardSq) v).setHighlight(event.getAction() == MotionEvent.ACTION_DOWN);
		return false;
	}
}
