/* GenesisChess, an Android chess application
 * Copyright 2022, Justin Madru (justin.jdm64@gmail.com)
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
import android.widget.*;
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;

public class PromoteView extends LinearLayout implements View.OnClickListener, View.OnTouchListener
{
	private final DisplayMetrics METRICS = new DisplayMetrics();
	private final BoardButton[] square = new BoardButton[4];
	private final PieceImgPainter painter;
	private final IGameController2 controller;

	private Move move;

	public PromoteView(Context context, IGameController2 Ctrl)
	{
		super(context, null);
		setOrientation(LinearLayout.VERTICAL);
		painter = new PieceImgPainter(context);
		controller = Ctrl;
		init();
	}

	private void init()
	{
		for (int i = 0, piece = Piece.QUEEN; i < 4;) {
			var row = new ManualPanel(getContext());
			row.setSizes("1/2");

			for (int j = 0; j < 2; piece--, j++, i++) {
				square[i] = new BoardButton(getContext(), painter, (i < 2)? j : j + 1);
				square[i].setOnClickListener(this);
				square[i].setOnTouchListener(this);
				row.addView(square[i]);
			}
			addView(row);
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		Util.getMetrics(METRICS, getContext());

		int size = 2 * Math.min(METRICS.heightPixels, METRICS.widthPixels) / 6;
		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);
		painter.resize(getMeasuredWidth() / 2);
	}

	public void setMove(Move _move, int color)
	{
		move = _move;
		for (int i = 0, piece = Piece.QUEEN; i < 4;) {
			for (int j = 0; j < 2; piece--, j++, i++) {
				square[i].setPiece(piece * color);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		var sq = (IBoardSq) v;
		var type = Math.abs(sq.getPiece());
		move.setPromote(type);
		controller.onPromoteClick(move);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		((ISquare) v).setHighlight(event.getAction() == MotionEvent.ACTION_DOWN);
		return false;
	}
}
