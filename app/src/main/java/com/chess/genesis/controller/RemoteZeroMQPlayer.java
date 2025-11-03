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
import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import com.chess.genesis.net.ZeroMQClient.*;
import com.chess.genesis.net.msgs.LastMoveMsg;

public class RemoteZeroMQPlayer extends LocalPlayer implements IMoveListener
{
	final String gameId;
	ZeroMQClient client;

	final LocalConnection connection = new LocalConnection()
	{
		@Override
		public void onServiceConnected(ZeroMQClient mqttClient)
		{
			client = mqttClient;
			client.listenMoves(gameId, RemoteZeroMQPlayer.this);
		}
	};

	public RemoteZeroMQPlayer(int YColor, IGameModel Model, Context context)
	{
		super(YColor, Model);
		var game = model.saveBoard();
		gameId = game.gameid;
		playerName = YColor == Piece.WHITE ? "(W) " + game.whiteName() : "(B) " + game.blackName();
		ZeroMQClient.bind(context, connection);
	}

	@Override
	public boolean canClick(int stm)
	{
		return false;
	}

	@Override
	public void reloadBoard(GameEntity data)
	{
		model.setBoard(data);
	}

	@Override
	public void onMove(LastMoveMsg moveMsg)
	{
		model.currentMove();

		var board = model.getBoard();
		var res = board.parseMove(moveMsg.move_str);
		if (res.second != Board.VALID_MOVE)
			return;
		model.applyMove(res.first, moveMsg.move_time);
	}

	@Override
	public void onDispose(Context context)
	{
		client.listenMoves(gameId, null);
		context.unbindService(connection);
	}
}
