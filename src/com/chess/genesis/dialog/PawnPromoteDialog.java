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

package com.chess.genesis.dialog;

import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;

public class PawnPromoteDialog extends BaseDialog
{
	public final static int MSG = 124;

	private final Context context;
	private final Handler handle;
	private final RegMove move;
	private final int color;

	public PawnPromoteDialog(final Context _context, final Handler handler, final RegMove _move, final int _color)
	{
		super(_context, BaseDialog.CANCEL);
		context = _context;
		handle = handler;
		move = _move;
		color = _color;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Pawn Promotion");
		setBodyView(R.layout.dialog_pawnpromote);

		setupPieces();
	}

	@Override
	public void onClick(final View v)
	{
		if (v instanceof BoardButton) {
			move.setPromote(Math.abs(((BoardButton) v).getPiece()));
			handle.sendMessage(handle.obtainMessage(MSG, move));
		}
		dismiss();
	}

	private void setupPieces()
	{
		final PromoteLayout table = (PromoteLayout) findViewById(R.id.table);
		table.init(context, this, color);
	}
}

class PromoteLayout extends LinearLayout implements OnClickListener, OnTouchListener
{
	private final BoardButton[] square = new BoardButton[4];
	private final PieceImgCache cache;
	private PawnPromoteDialog dialog;
	private int color;

	public PromoteLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		cache = new PieceImgCache(context, PieceImgCache.PIECE_ONLY);
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int size = Math.min(320, Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)));
		super.onMeasure(MeasureSpec.AT_MOST | size, MeasureSpec.AT_MOST | size);
		cache.resize(getMeasuredWidth() / 2);
	}

	public void init(final Context context, final PawnPromoteDialog _dialog, final int _color)
	{
		dialog = _dialog;
		color = _color;

		for (int i = 0, piece = Piece.QUEEN; i < 4;) {
			final ManualPanel row = new ManualPanel(context);
			row.setSizes("1,1/2");

			for (int j = 0; j < 2; piece--, j++, i++) {
				square[i] = new BoardButton(context, cache, (i < 2)? j : j + 1);
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
		((BoardButton) v).setHighlight(event.getAction() == MotionEvent.ACTION_DOWN);
		return false;
	}
}
