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
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import androidx.compose.runtime.*;

public class LocalMqttPlayer extends LocalPlayer
{
	String gameId;
	MutableState<SubmitState> submitState;

	public LocalMqttPlayer(int YColor, IGameModel Model, MutableState<SubmitState> SubmitState)
	{
		super(YColor, Model);
		gameId = model.saveBoard().gameid;
		submitState = SubmitState;
	}

	@Override
	public boolean canClick(int stm)
	{
		return yColor == stm && model.isCurrentMove();
	}

	@Override
	public void finalizeMove(Move move, Context context)
	{
		submitState.setValue(new SubmitState(move));
	}

	@Override
	public void submitMove(Move move, Context context)
	{
		AdhocMqttClient.bind(context, (client) -> client.sendMove(gameId, yColor, model.getHistory().size(), move));
		super.finalizeMove(move, context);
	}
}
