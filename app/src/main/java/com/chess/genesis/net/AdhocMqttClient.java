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
package com.chess.genesis.net;

import java.util.*;
import android.app.*;
import android.content.*;
import android.os.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.*;
import com.chess.genesis.data.*;
import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;

public class AdhocMqttClient extends Service implements MqttCallback
{
	private final static String URL = "tcp://broker.hivemq.com:1883";
	private final static String BASE_TOPIC = "genchess/adhoc";

	private static AdhocMqttClient INST = null;

	MqttClient client;
	Map<String,IMoveListener> moveListeners = new HashMap<>();

	public interface IMoveListener
	{
		void onMove(MoveMsg msg);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		try {
			var ctx = getApplicationContext();
			var id = "Android-" + Pref.getUUID(ctx);
			var persistence = new MqttDefaultFilePersistence(ctx.getCacheDir().getAbsolutePath());
			client = new MqttClient(URL, id, persistence);
			client.setCallback(this);

			INST = this;
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		connect();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		INST = null;
	}

	public static synchronized AdhocMqttClient get(Context ctx)
	{
		if (INST == null) {
			ctx.startService(new Intent(ctx, AdhocMqttClient.class));
		}
		return INST;
	}

	public void sendInvite(LocalGameEntity data)
	{
		var color = data.opponent == Enums.INVITE_WHITE_OPPONENT ? Piece.WHITE : Piece.BLACK;
		sendInvite(data.gameid, data.gametype, color);
	}

	private void sendInvite(String gameId, int type, int color)
	{
		publish(inviteTopic(gameId), InviteMsg.write(type, color), true);
	}

	public void listenInvite(String gameId)
	{
		subscribe(inviteTopic(gameId));
	}

	public void listenMoves(String gameId, int color, IMoveListener listener)
	{
		setMoveListener(gameId, listener);
		subscribe(movesTopic(gameId, color));
	}

	public void sendMove(String gameId, int color, int index, Move move)
	{
		publish(movesTopic(gameId, color), MoveMsg.write(move, index), true);
	}

	public void setMoveListener(String gameId, IMoveListener listener)
	{
		moveListeners.put(gameId, listener);
	}

	private static String inviteTopic(String gameId)
	{
		return gameId + "/i";
	}

	private static String movesTopic(String gameId, int color)
	{
		return gameId + "/" + (color == Piece.WHITE ? "w" : "b");
	}

	private void publish(String topic, byte[] data, boolean retained)
	{
		try {
			connect();
			client.publish(BASE_TOPIC + "/" + topic, data, 1, retained);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	private void subscribe(String topic)
	{
		try {
			connect();
			client.subscribe(BASE_TOPIC + "/" + topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	private void connect()
	{
		if (!client.isConnected()) {
			try {
				client.connect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void connectionLost(Throwable cause)
	{
		cause.printStackTrace();
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception
	{
		if (!topic.startsWith(BASE_TOPIC)) {
			return;
		}

		var parts = topic.split("/");
		var gameId = parts[2];
		var chan = parts[3];

		switch (chan) {
		case "i":
			var msg = InviteMsg.parse(message);
			LocalGameDao.get(getApplicationContext()).importInviteGame(gameId, msg.type, msg.color);
			break;
		case "w":
		case "b":
			var moveMsg = MoveMsg.parse(message);
			LocalGameDao.get(getApplicationContext()).saveMove(gameId, moveMsg.index, moveMsg.move);

			var listener = moveListeners.get(gameId);
			if (listener != null) {
				listener.onMove(moveMsg);
			}
			break;
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{

	}
}
