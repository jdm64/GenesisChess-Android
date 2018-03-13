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

import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.os.*;
import android.view.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;
import java.util.Map.*;

public class PawnPromoteDialog extends DialogFragment implements View.OnClickListener, DialogInterface.OnClickListener
{
	public final static int MSG = 124;

	private Handler handle;
	private Move move;
	private int color;

	public static PawnPromoteDialog create(Handler handler, Move move, int color)
	{
		PawnPromoteDialog dialog = new PawnPromoteDialog();
		dialog.handle = handler;
		dialog.move = move;
		dialog.color = color;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_pawnpromote);

		builder.getValue()
				.setTitle("Pawn Promotion")
				.setNegativeButton("Cancel", this);

		PromoteLayout table = builder.getKey().findViewById(R.id.table);
		table.init(getActivity(), this, color);

		return builder.getValue().create();
	}

	@Override
	public void onClick(View v)
	{
		if (v instanceof IBoardSq) {
			move.setPromote(Math.abs(((IBoardSq) v).getPiece()));
			handle.sendMessage(handle.obtainMessage(MSG, move));
		}
		dismiss();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		dismiss();
	}
}
