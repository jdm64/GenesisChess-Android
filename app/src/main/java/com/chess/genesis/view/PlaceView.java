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
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;

public class PlaceView extends LinearLayout implements OnClickListener
{
	public static final LayoutParams LINEAR_PARAMS =
		new LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

	final static int[] TYPES = {
		Piece.EMPTY, Piece.WHITE_KING, Piece.WHITE_QUEEN, Piece.WHITE_ROOK, Piece.EMPTY,
		Piece.EMPTY, Piece.BLACK_KING, Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.EMPTY,
		Piece.EMPTY, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT, Piece.WHITE_PAWN, Piece.EMPTY,
		Piece.EMPTY, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT, Piece.BLACK_PAWN, Piece.EMPTY
	};

	private final static int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	private final DisplayMetrics METRICS = new DisplayMetrics();
	private final PieceImgView[] squares = new PieceImgView[13];
	private final PieceImgPainter painter;
	private final IGameController controller;

	public PlaceView(Context context, IGameController Ctrl)
	{
		super(context, null);
		setOrientation(LinearLayout.VERTICAL);
		painter = new PieceImgPainter(context);
		controller = Ctrl;
		init();
	}

	private void init()
	{
		for (int i = 0; i < TYPES.length;) {
			var row = new ManualPanel(getContext());
			row.setSizes("1/10");
			addView(row);

			for (int j = 0; j < TYPES.length / 2; j++, i++) {
				var type = TYPES[i];
				var count = typeCounts[Math.abs(type)];
				var index = type + IGameModel.PLACEOFFSET;
				var view = new PieceImgView(getContext(), painter, index, type, count, false, false);
				if (type != Piece.EMPTY) {
					var idx = type + 6;
					squares[idx] = view;
					squares[idx].setOnClickListener(this);
				}
				row.addView(view);
			}

			squares[6] = new PieceImgView(getContext(), painter, 0, Piece.EMPTY, 0, false, false);
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		Util.getMetrics(METRICS, getContext());

		var size = Math.min(METRICS.heightPixels, METRICS.widthPixels);
		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);
		painter.resize(getMeasuredWidth() / (TYPES.length / 2));
	}

	public void setPieces(int[] counts)
	{
		for (int i = 0; i < counts.length; i++) {
			squares[i].setCount(counts[i]);
		}
	}

	public ICountSq getPiece(int index)
	{
		var type = index - Board.PLACEOFFSET;
		return squares[type + 6];
	}

	@Override
	public void onClick(View v)
	{
		var sq = (ICountSq) v;
		if (sq.getCount() > 0) {
			controller.onPlaceClick(sq);
		}
	}
}
