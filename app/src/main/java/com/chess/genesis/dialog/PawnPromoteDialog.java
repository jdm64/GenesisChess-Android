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

package com.chess.genesis.dialog;

import android.content.*;
import android.os.*;
import android.view.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;

public class PawnPromoteDialog extends BaseDialog
{
	public final static int MSG = 124;

	private final Context context;
	private final Handler handle;
	private final Move move;
	private final int color;

	public PawnPromoteDialog(final Context _context, final Handler handler, final Move _move, final int _color)
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
		if (v instanceof IBoardSq) {
			move.setPromote(Math.abs(((IBoardSq) v).getPiece()));
			handle.sendMessage(handle.obtainMessage(MSG, move));
		}
		dismiss();
	}

	private void setupPieces()
	{
		final PromoteLayout table = findViewById(R.id.table);
		table.init(context, this, color);
	}
}
