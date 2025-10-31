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
package com.chess.genesis.controller;

import android.content.*;
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import androidx.compose.runtime.*;

public class LocalZeroMQPlayer extends LocalPlayer
{
	final String gameId;
	final MutableState<SubmitState> submitState;

	public LocalZeroMQPlayer(int YColor, IGameModel Model, MutableState<SubmitState> SubmitState)
	{
		super(YColor, Model);
		gameId = model.saveBoard().gameid;
		playerName = (YColor == Piece.WHITE ? "(W)" : "(B)") + " You";
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
		ZeroMQClient.bind(context, (client) -> client.sendMove(gameId, move.toString()));
		super.finalizeMove(move, context);
	}
}
