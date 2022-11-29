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
import com.chess.genesis.*;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;

public class ComputerPlayer extends LocalPlayer
{
	private final Engine engine;

	public ComputerPlayer(int ycol, IGameModel model)
	{
		super(ycol, model);
		engine = Engine.create(model.getBoard());
	}

	@Override
	public String getStmName(boolean overwrite)
	{
		var name = super.getStmName(overwrite);
		if (overwrite && model.getBoard().getStm() == yColor)
			name = name + " (thinking)";
		return name;
	}

	@Override
	public void finalizeMove(Move move, Context context)
	{
		engine.setEndTime(0);
		super.finalizeMove(move, context);
	}

	@Override
	public void takeTurn(Context context)
	{
		var cpuTime = Pref.getInt(context, R.array.pf_cpuTime);
		Util.runThread(() -> runEngine(cpuTime));
	}

	private void runEngine(int cpuTime)
	{
		var board = model.getBoard();
		engine.setBoard(board);
		var move = engine.getMove(cpuTime);
		if (engine.getEndTime() == 0) {
			Util.runThread(() -> runEngine(cpuTime));
			return;
		}

		model.currentMove();

		var vMove = new Move();
		if (board.validMove(move, vMove))
			model.applyMove(vMove, true);
	}
}
