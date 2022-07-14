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
import com.chess.genesis.net.*;
import com.chess.genesis.net.AdhocMqttClient.*;
import com.chess.genesis.engine.*;

public class RemoteMqttPlayer extends LocalPlayer implements IMoveListener
{
	String gameId;
	AdhocMqttClient client;

	LocalConnection connection = new LocalConnection()
	{
		@Override
		public void onServiceConnected(AdhocMqttClient mqttClient)
		{
			client = mqttClient;
			client.listenMoves(gameId, yColor, RemoteMqttPlayer.this);
		}
	};

	public RemoteMqttPlayer(int YColor, IGameModel Model, Context context)
	{
		super(YColor, Model);
		gameId = model.saveBoard().gameid;
		AdhocMqttClient.bind(context, connection);
	}

	@Override
	public boolean canClick(int stm)
	{
		return false;
	}

	@Override
	public void onMove(MoveMsg msg)
	{
		model.currentMove();

		var board = model.getBoard();
		var res = board.parseMove(msg.move);
		if (res.second != Move.VALID_MOVE)
			return;
		model.applyMove(res.first, true);
	}

	@Override
	public void onDispose(Context context)
	{
		client.setMoveListener(gameId, null);
		context.unbindService(connection);
	}
}
