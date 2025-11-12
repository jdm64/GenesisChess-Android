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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import android.app.*;
import android.content.Context;
import android.content.*;
import android.os.*;
import org.zeromq.*;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.*;
import org.zeromq.ZMQException;
import com.chess.genesis.*;
import com.chess.genesis.controller.*;
import com.chess.genesis.data.*;
import com.chess.genesis.db.*;
import com.chess.genesis.net.msgs.*;
import com.chess.genesis.util.*;
import zmq.*;

public class ZeroMQClient extends Service
{
	private final static long INACTIVITY_TIMEOUT = 2 * 60 * 1000L;
	private final static long CONNECTION_TIMEOUT = 5 * 1000L;

	static final AtomicBoolean appActive = new AtomicBoolean(false);

	final LocalBinder binder = new LocalBinder();
	final LinkedBlockingQueue<ZmqMsg> sendQueue = new LinkedBlockingQueue<>();
	final Map<String, IMoveListener> moveListeners = new ConcurrentHashMap<>();

	ZContext ctx;
	Socket socket;
	Socket monitorSock;
	AtomicLong lastPing = new AtomicLong();
	AtomicLong lastPong = new AtomicLong();
	AtomicLong lastReceive = new AtomicLong();
	AtomicReference<Status> status = new AtomicReference<>(Status.DISCONNECTED);
	AtomicBoolean isLoggedin = new AtomicBoolean(false);
	Future<?> receiveFuture;
	Future<?> inactivityFuture;
	Future<?> sendFuture;
	Future<?> monitorFuture;

	public enum Status
	{
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}

	public interface IMoveListener
	{
		void reloadBoard(GameEntity data);

		void onMove(LastMoveMsg moveMsg);
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
		ctx.startService(new Intent(ctx, ZeroMQClient.class));
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
				client.showConnectionError();
				ctx.unbindService(this);
			}
		});
	}

	public static void setAppActive(boolean active)
	{
		appActive.set(active);
	}

	public void showConnectionError()
	{
		if (status.get() == Status.CONNECTED) {
			return;
		}

		Util.runThread(() -> {
			try {
				Thread.sleep(CONNECTION_TIMEOUT);
				if (status.get() != Status.CONNECTED) {
					Util.showToast("Error connecting to server", this);
				}
			} catch (InterruptedException e) {
				// ignore
			}
		});
	}

	private void connect()
	{
		try {
			status.set(Status.CONNECTING);
			lastReceive.set(System.currentTimeMillis());
			ctx = new ZContext();
			socket = ctx.createSocket(SocketType.DEALER);

			var host = Pref.getString(this, R.array.pf_serverhost);
			if (host == null || host.isBlank()) {
				var pref = new PrefEdit(this).putString(R.array.pf_serverhost).commit();
				host = pref.getString(R.array.pf_serverhost);
			}
			Util.log("Connecting to: " + host, this);

			socket.connect(host);
			socket.monitor("inproc://monitor", ZMQ.EVENT_CONNECTED | ZMQ.EVENT_DISCONNECTED | ZMQ.EVENT_CONNECT_DELAYED);
			monitorSock = ctx.createSocket(SocketType.PAIR);
			monitorSock.connect("inproc://monitor");
			monitorFuture = Util.runThread(this::monitorLoop);
			receiveFuture = Util.runThread(this::receiveLoop);
		} catch (Throwable e) {
			Util.logErr(e, this);
			Util.showToast("Server connection failed", this);
			disconnect();
		}
	}

	private synchronized void disconnect()
	{
		if (ctx == null) {
			return;
		}

		Util.log("Starting disconnect", this);

		isLoggedin.set(false);
		moveListeners.clear();
		status.set(Status.DISCONNECTED);

		if (monitorSock != null) {
			monitorSock.close();
			monitorSock = null;
		}
		if (socket != null) {
			socket.close();
			socket = null;
		}
		if (ctx != null) {
			ctx.close();
			ctx = null;
		}
		if (monitorFuture != null) {
			monitorFuture.cancel(true);
			monitorFuture = null;
		}
		if (receiveFuture != null) {
			receiveFuture.cancel(true);
			receiveFuture = null;
		}
		if (inactivityFuture != null) {
			inactivityFuture.cancel(true);
			inactivityFuture = null;
		}

		Util.log("Disconnect finished", this);
	}

	private void reconnect()
	{
		disconnect();
		connect();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	@Override
	public void onCreate()
	{
		Util.log("Starting service", this);
		sendFuture = Util.runThread(this::sendLoop);
	}

	@Override
	public void onDestroy()
	{
		Util.log("Shutting down service", this);
		if (sendFuture != null) {
			sendFuture.cancel(true);
			sendFuture = null;
		}
		disconnect();
	}

	public synchronized void listenMoves(String gameId, RemoteZeroMQPlayer player)
	{
		if (player == null) {
			moveListeners.remove(gameId);
			return;
		}

		var last = moveListeners.put(gameId, player);
		if (socket == null) {
			connect();
		}

		if (last != player) {
			getActiveData(gameId);
			showConnectionError();
		}
	}

	public void ping()
	{
		send(PingMsg.build());
		lastPing.set(System.currentTimeMillis());
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
		if (isLoggedin.get()) {
			return;
		}

		var ctx = getApplicationContext();
		var account = Pref.getUserPass(ctx);
		if (account != null) {
			login(account.getKey(), account.getValue());
		} else {
			registerAnon(Pref.newAnonHash(ctx));
		}
	}

	private void send(ZmqMsg msg)
	{
		try {
			sendQueue.put(msg);
		} catch (InterruptedException e) {
			Util.logErr(e, this);
		}
	}

	private void receiveLoop()
	{
		inactivityFuture = Util.runThread(this::inactivityLoop);
		while (socket != null) {
			try {
				var msg = ZmqMsg.parse(socket.recv());
				lastReceive.set(System.currentTimeMillis());

				switch (msg.type()) {
				case PingMsg.ID:
					socketSend(PongMsg.build(msg.as(PingMsg.class)));
					break;
				case AnonAcctMsg.ID:
					var acct = msg.as(AnonAcctMsg.class);
					Pref.storeAnonUser(getApplicationContext(), acct.name);
					isLoggedin.set(true);
					break;
				case LoginResultMsg.ID:
					var result = msg.as(LoginResultMsg.class);
					isLoggedin.set(result.is_ok);
					break;
				case ActiveGameDataMsg.ID:
					var game = msg.as(ActiveGameDataMsg.class);
					var ctx = getApplicationContext();
					var dao = ActiveGameDao.get(ctx);
					if (game.is_new) {
						dao.importInviteGame(game, ctx);
					} else {
						var gameData = dao.updateActiveGame(game, ctx);
						if (gameData == null) {
							break;
						}
						var listener = moveListeners.get(game.game_id);
						if (listener != null) {
							listener.reloadBoard(gameData);
						}
					}
					break;
				case LastMoveMsg.ID:
					var moveMsg = msg.as(LastMoveMsg.class);
					ActiveGameDao.get(getApplicationContext()).saveMove(moveMsg);

					var listener = moveListeners.get(moveMsg.game_id);
					if (listener != null) {
						listener.onMove(moveMsg);
					}
					break;
				case PongMsg.ID:
					lastPong.set(System.currentTimeMillis());
					break;
				case OkMsg.ID:
					break;
				case ErrorMsg.ID:
					var errMsg = msg.as(ErrorMsg.class);
					Util.showToast(errMsg.msg, getApplicationContext());
					break;
				case UnknownMsg.ID:
				default:
					Util.logErr("Unexpected message: " + msg, this);
					break;
				}
			} catch (ZMQException ze) {
				var err = ze.getErrorCode();
				if (err == ZError.ETERM) {
					Util.log("ZMQ context terminated", this);
				} else if (err == ZError.EINTR) {
					Util.log("Socket interrupted because shutting down", this);
				} else {
					Util.logErr(ze, this);
				}
				break;
			} catch (Throwable e) {
				Util.logErr(e, this);
				break;
			}
		}
		Util.log("Shutting down receiveLoop", this);
	}

	private void sendLoop()
	{
		while (sendFuture != null) {
			try {
				var msg = sendQueue.poll(5, TimeUnit.SECONDS);
				socketSend(msg);
			} catch (Throwable e) {
				Util.logErr(e, this);
				break;
			}
		}
		Util.log("Shutting down sendLoop", this);
	}

	private void socketSend(ZmqMsg msg) throws IOException
	{
		if (msg == null) {
			return;
		}
		if (socket == null) {
			reconnect();
		}

		socket.send(msg.toBytes());
		Util.log("Sent message: " + msg, this);
	}

	private void inactivityLoop()
	{
		while (socket != null) {
			try {
				Thread.sleep(CONNECTION_TIMEOUT);
				if (!appActive.get() && sendQueue.isEmpty() && System.currentTimeMillis() - lastReceive.get() > INACTIVITY_TIMEOUT) {
					disconnect();
				}
			} catch (Throwable e) {
				Util.logErr(e, this);
				break;
			}
		}
		Util.log("Shutting down inactivityLoop", this);
	}

	private void monitorLoop()
	{
		while (monitorSock != null) {
			try {
				var event = ZMQ.Event.recv(monitorSock);
				if (event == null) {
					status.set(Status.DISCONNECTED);
					continue;
				}

				switch (event.getEvent()) {
				case ZMQ.EVENT_CONNECTED:
					status.set(Status.CONNECTED);
					break;
				case ZMQ.EVENT_DISCONNECTED:
					status.set(Status.DISCONNECTED);
					break;
				case ZMQ.EVENT_CONNECT_DELAYED:
					status.set(Status.CONNECTING);
					break;
				}
			} catch (Throwable e) {
				Util.logErr(e, this);
				break;
			}
		}
		Util.log("Shutting down monitorLoop", this);
	}
}
