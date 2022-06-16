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
import android.util.*;
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;

public class LocalMqttPlayer extends LocalPlayer
{
	AdhocMqttClient client;
	String gameId;

	public LocalMqttPlayer(int YColor, IGameModel Model, AdhocMqttClient Client)
	{
		super(YColor, Model);
		gameId = model.saveBoard().gameid;
		client = Client;
	}

	@Override
	public boolean canClick(int stm)
	{
		return yColor == stm && model.isCurrentMove();
	}

	@Override
	public void finalizeMove(Move move, Context context)
	{
		client.sendMove(gameId, yColor, model.getHistory().size(), move);
		super.finalizeMove(move, context);
	}
}
