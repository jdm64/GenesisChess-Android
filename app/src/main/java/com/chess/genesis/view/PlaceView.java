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
import android.view.View.*;
import android.widget.*;
import com.chess.genesis.api.*;
import com.chess.genesis.controller.*;
import com.chess.genesis.engine.*;

public class PlaceView extends LinearLayout implements OnClickListener, OnTouchListener
{
	public static final LayoutParams LINEAR_PARAMS =
		new LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

	private final PlaceButton[] squares = new PlaceButton[6];

	private IGameController2 controller;
	private final PieceImgPainter painter;

	public PlaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		painter = new PieceImgPainter(context);
		init();
	}

	private void init()
	{
		for (int i = 0, idx = Piece.KING; i < 6;) {
			var row = new ManualPanel(getContext());
			row.setSizes("1/3");

			for (int j = 0; j < 3; j++, i++) {
				squares[i] = new PlaceButton(getContext(), painter, idx--, false);
				squares[i].setOnClickListener(this);
				squares[i].setOnTouchListener(this);
				squares[i].setId(i);
				row.addView(squares[i]);
			}
			addView(row);
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		var wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		var dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);

		var size = 3 * Math.min(dm.heightPixels, dm.widthPixels) / 6;
		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);
		painter.resize(getMeasuredWidth() / 3);
	}

	public void setController(IGameController2 gameController)
	{
		controller = gameController;
	}

	public void setPieces(int[] counts, int stm)
	{
		for (int i = 0, piece = Piece.KING; i < 6; i++, piece--) {
			var type = piece * stm;
			var count = counts[type + 6];
			squares[i].setPiece(count > 0 ? type : Piece.EMPTY);
			squares[i].setCount(count);
		}
	}

	public IPlaceSq getPiece(int index)
	{
		var type = index - GenGameModel.PLACEOFFSET;
		return squares[6 - Math.abs(type)];
	}

	@Override
	public void onClick(View v)
	{
		var sq = (IPlaceSq) v;
		if (sq.getCount() > 0) {
			controller.onPlaceClick(sq);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		var sq = (IPlaceSq) v;
		if (sq.getCount() > 0) {
			sq.setHighlight(event.getAction() == MotionEvent.ACTION_DOWN);
		}
		return false;
	}
}
