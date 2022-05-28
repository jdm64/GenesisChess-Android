/* GenChess, a genesis chess engine
 * Copyright (C) 2022, Justin Madru (justin.jdm64@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chess.genesis.controller;

import android.content.*;
import com.chess.genesis.api.*;
import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;

public class LocalPlayer implements IPlayer
{
	protected IGameModel model;
	protected int yColor;

	public LocalPlayer(int color, IGameModel _model)
	{
		yColor = color;
		model = _model;
	}

	@Override
	public boolean canClick(int stm)
	{
		return yColor == stm;
	}

	@Override
	public String getStmName(boolean overwrite)
	{
		return yColor == Piece.WHITE ? "White" : "Black";
	}

	@Override
	public void finalizeMove(Move move, Context context)
	{
		Util.runThread(() -> {
			var data = model.saveBoard();
			LocalGameDao.get(context).update((LocalGameEntity) data);
		});
	}

	@Override
	public void takeTurn()
	{
		// Do nothing
	}
}
