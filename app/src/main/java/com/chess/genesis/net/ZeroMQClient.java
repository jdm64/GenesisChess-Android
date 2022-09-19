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
package com.chess.genesis.net;

import java.util.*;
import java.util.concurrent.*;
import android.app.*;
import android.content.*;
import android.content.Context;
import android.os.*;
import android.util.*;
import org.zeromq.*;
import org.zeromq.ZMQ.*;
import com.chess.genesis.*;
import com.chess.genesis.controller.*;
import com.chess.genesis.data.*;
import com.chess.genesis.db.*;
import com.chess.genesis.net.msgs.*;
import com.chess.genesis.util.*;

public class ZeroMQClient extends Service
{
	private final static String ANON = "anonymous";

	LocalBinder binder = new LocalBinder();
	LinkedBlockingQueue<ZmqMsg> sendQueue = new LinkedBlockingQueue<>();
	Map<String, IMoveListener> moveListeners = new HashMap<>();

	Socket socket;
	boolean isLoggedin = false;

	public interface IMoveListener
	{
		void onMove(String moveStr, int idx);
	}

	public interface RunCommand
	{
		void run(ZeroMQClient client);
	}

	public class LocalBinder extends Binder
	{
		public ZeroMQClient get()
		{
			return ZeroMQClient.this;
		}
	}

	public static abstract class LocalConnection implements ServiceConnection
	{
		public abstract void onServiceConnected(ZeroMQClient client);

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Util.runThread(() -> onServiceConnected(((LocalBinder) service).get()));
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
		}
	}

	public static void bind(Context ctx, LocalConnection connection)
	{
		ctx.bindService(new Intent(ctx, ZeroMQClient.class), connection, Context.BIND_AUTO_CREATE);
	}

	public static void bind(Context ctx, RunCommand command)
	{
		bind(ctx, new LocalConnection()
		{
			@Override
			public void onServiceConnected(ZeroMQClient client)
			{
				command.run(client);
				ctx.unbindService(this);
			}
		});
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	@Override
	public void onCreate()
	{
		Util.runThread(() -> {
			try (var ctx = new ZContext()) {
				socket = ctx.createSocket(SocketType.DEALER);
				socket.connect("tcp://jdserver.org:1993");
				Util.runThread(this::sendLoop);
				recieveLoop();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onDestroy()
	{
		isLoggedin = false;
	}

	public void listenMoves(String gameId, RemoteZeroMQPlayer player)
	{
		moveListeners.put(gameId, player);
		if (player != null) {
			getActiveData(gameId);
		}
	}

	public void register(String username, String hash)
	{
		send(RegisterMsg.build(username, hash));
	}

	public void registerAnon(String hash)
	{
		send(RegisterAnonMsg.build(hash));
	}

	public void login(String username, String hash)
	{
		send(LoginMsg.build(username, hash));
	}

	public void createInvite(int gameType, int playAs)
	{
		do_login();
		send(CreateInviteMsg.build(gameType, playAs));
	}

	public void getActiveData(String gameId)
	{
		send(GetActiveDataMsg.build(gameId));
	}

	public void joinInvite(String gameId)
	{
		do_login();
		send(JoinInviteMsg.build(gameId));
	}

	public void sendMove(String gameId, String moveStr)
	{
		do_login();
		send(MakeMoveMsg.build(gameId, moveStr));
	}

	private synchronized void do_login()
	{
		if (isLoggedin) {
			return;
		}

		var ctx = getApplicationContext();
		if (ANON.equals(Pref.getString(ctx, R.array.pf_isLoggedIn))) {
			var username = Pref.getString(ctx, R.array.pf_anon_username);
			var hash = Pref.getString(ctx, R.array.pf_anon_passhash);
			login(username, hash);
		} else {
			var hash = Util.getSUID(40);
			new PrefEdit(ctx).putString(R.array.pf_anon_passhash, hash).commit();
			registerAnon(hash);
		}
	}

	private void send(ZmqMsg msg)
	{
		try {
			sendQueue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void recieveLoop()
	{
		while (true) {
			try {
				var msg = ZmqMsg.parse(socket.recv());
				switch (msg.type()) {
				case PingMsg.ID:
					socket.send(PongMsg.build(msg.as(PingMsg.class)).toBytes());
					break;
				case AnonAcctMsg.ID:
					var acct = msg.as(AnonAcctMsg.class);
					new PrefEdit(getApplicationContext())
					    .putString(R.array.pf_username, acct.name)
					    .putString(R.array.pf_anon_username, acct.name)
					    .putString(R.array.pf_isLoggedIn, ANON)
					    .commit();
					isLoggedin = true;
					break;
				case LoginResultMsg.ID:
					var result = msg.as(LoginResultMsg.class);
					isLoggedin = result.is_ok;
					break;
				case ActiveGameDataMsg.ID:
					var game = msg.as(ActiveGameDataMsg.class);
					var ctx = getApplicationContext();
					var dao = LocalGameDao.get(ctx);
					if (game.is_new) {
						dao.importInviteGame(game, ctx);
					} else {
						var newMoves = dao.updateInviteGame(game, ctx);
						var listener = moveListeners.get(game.game_id);
						if (listener != null) {
							for (var newMove : newMoves) {
								listener.onMove(newMove.first, newMove.second);
							}
						}
					}
					break;
				case LastMoveMsg.ID:
					var moveMsg = msg.as(LastMoveMsg.class);
					LocalGameDao.get(getApplicationContext()).saveMove(moveMsg);

					var listener = moveListeners.get(moveMsg.game_id);
					if (listener != null) {
						listener.onMove(moveMsg.move_str, moveMsg.move_idx);
					}
					break;
				case OkMsg.ID:
				case PongMsg.ID:
					break;
				case ErrorMsg.ID:
				case UnknownMsg.ID:
				default:
					Log.e(getClass().getSimpleName(), "Unexpected message: " + msg.toString());
					break;
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void sendLoop()
	{
		while (true) {
			try {
				var msg = sendQueue.take();
				if (msg != null) {
					socket.send(msg.toBytes());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
