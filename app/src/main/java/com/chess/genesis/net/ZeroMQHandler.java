package com.chess.genesis.net;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import android.content.*;
import com.chess.genesis.controller.*;
import com.chess.genesis.data.*;
import com.chess.genesis.data.Enums.*;
import com.chess.genesis.db.*;
import com.chess.genesis.net.msgs.*;
import com.chess.genesis.util.*;

public class ZeroMQHandler
{
	public interface IMoveListener
	{
		void reloadBoard(GameEntity data);

		void onMove(LastMoveMsg moveMsg);

		void onResult(GameResultMsg resultMsg);
	}

	public interface IPingListener
	{
		void onPong(PongMsg msg, long pingTime);
	}

	private final ZeroMQClient client;

	final AtomicBoolean isLoggedIn = new AtomicBoolean(false);
	final AtomicBoolean hasSynced = new AtomicBoolean(false);
	final Map<String, IMoveListener> moveListeners = new ConcurrentHashMap<>();
	final Set<IPingListener> pingListeners = ConcurrentHashMap.newKeySet();
	final AtomicLong lastPing = new AtomicLong();
	final AtomicLong lastPong = new AtomicLong();

	ZeroMQHandler(ZeroMQClient client)
	{
		this.client = client;
	}

	void clear()
	{
		isLoggedIn.set(false);
		hasSynced.set(false);
		moveListeners.clear();
		pingListeners.clear();
	}

	Context getContext()
	{
		return client.getApplicationContext();
	}

	public void handle(ZmqMsg msg)
	{
		switch (msg.type()) {
		case PingMsg.ID:
			handlePing(msg.as(PingMsg.class));
			break;
		case AnonAcctMsg.ID:
			handleAnonAcct(msg.as(AnonAcctMsg.class));
			break;
		case LoginResultMsg.ID:
			handleLoginResult(msg.as(LoginResultMsg.class));
			break;
		case ActiveGameDataMsg.ID:
			handleActiveGameData(msg.as(ActiveGameDataMsg.class));
			break;
		case ArchiveGameDataMsg.ID:
			handleArchiveGameData(msg.as(ArchiveGameDataMsg.class));
			break;
		case LastMoveMsg.ID:
			handleLastMove(msg.as(LastMoveMsg.class));
			break;
		case GameResultMsg.ID:
			handleGameResult(msg.as(GameResultMsg.class));
			break;
		case PongMsg.ID:
			handlePong(msg.as(PongMsg.class));
			break;
		case OkMsg.ID:
			handleOk(msg.as(OkMsg.class));
			break;
		case ErrorMsg.ID:
			handleError(msg.as(ErrorMsg.class));
			break;
		case GamesListMsg.ID:
			handleGamesList(msg.as(GamesListMsg.class));
			break;
		case UnknownMsg.ID:
		default:
			handleUnknown(msg);
			break;
		}
	}

	void handlePing(PingMsg msg)
	{
		client.send(PongMsg.build(msg));
	}

	void handleAnonAcct(AnonAcctMsg msg)
	{
		Pref.storeAnonUser(getContext(), msg.name);
		isLoggedIn.set(true);
	}

	void handleLoginResult(LoginResultMsg msg)
	{
		isLoggedIn.set(msg.is_ok);
		if (!msg.is_ok) {
			Util.showToast(msg.msg, getContext());
		}
	}

	void handleActiveGameData(ActiveGameDataMsg msg)
	{
		var ctx = getContext();
		var dao = ActiveGameDao.get(ctx);
		handleBoardReload(dao.update(msg, ctx));
	}

	void handleArchiveGameData(ArchiveGameDataMsg msg)
	{
		var ctx = getContext();
		var dao = ArchiveGameDao.get(ctx);
		handleBoardReload(dao.update(msg));
	}

	void handleLastMove(LastMoveMsg msg)
	{
		ActiveGameDao.get(getContext()).saveMove(msg);

		var listener = moveListeners.get(msg.id);
		if (listener != null) {
			listener.onMove(msg);
		}
	}

	void handleGameResult(GameResultMsg msg)
	{
		ArchiveGameDao.get(getContext()).copyFromActive(msg, getContext());

		var listener = moveListeners.get(msg.id);
		if (listener != null) {
			listener.onResult(msg);
		}
	}

	void handlePong(PongMsg msg)
	{
		lastPong.set(System.currentTimeMillis());
		var pingTime = lastPong.get() - lastPing.get();
		pingListeners.forEach(l -> {
			l.onPong(msg, pingTime);
		});
	}

	void handleOk(OkMsg msg)
	{
	}

	void handleError(ErrorMsg msg)
	{
		Util.log("ErrorMsg: " + msg.msg, client);
		Util.showToast(msg.msg, getContext());
	}

	void handleGamesList(GamesListMsg msg)
	{
		if (msg.mode == SyncType.ARCHIVE.id) {
			// TODO sync archive games
			return;
		}

		var ctx = getContext();
		var dao = ActiveGameDao.get(ctx);
		var existingSet = new HashSet<>(dao.getAllGameIds());

		msg.list.forEach(gameId -> {
			if (!existingSet.contains(gameId)) {
				getActiveData(gameId);
			}
		});
	}

	void handleUnknown(ZmqMsg msg)
	{
		Util.logErr("Unexpected message: " + msg, client);
	}

	void handleBoardReload(GameEntity data)
	{
		if (data == null) {
			return;
		}

		var listener = moveListeners.get(data.gameid);
		if (listener != null) {
			listener.reloadBoard(data);
		}
	}

	private Entry<String, String> getUserPass()
	{
		return Pref.getUserPass(getContext());
	}

	private synchronized void do_login()
	{
		if (isLoggedIn.get()) {
			return;
		}

		var account = getUserPass();
		if (account != null) {
			login(account.getKey(), account.getValue());
		} else {
			registerAnon(Pref.newAnonHash(getContext()));
		}
	}

	public void reconnect()
	{
		client.tryConnect();
	}

	public void showConnectionError()
	{
		client.showConnectionError();
	}

	public void register(String username, String hash)
	{
		client.send(RegisterMsg.build(username, hash));
	}

	public void registerAnon(String hash)
	{
		client.send(RegisterAnonMsg.build(hash));
	}

	public void login(String username, String hash)
	{
		client.send(LoginMsg.build(username, hash));
	}

	public void createInvite(GameType gameType, ColorType playAs, ClockType clockType, int baseTime, int incTime)
	{
		do_login();
		client.send(CreateInviteMsg.build(gameType, playAs, clockType, baseTime, incTime));
	}

	public void joinMatched(GameType gameType, ColorType playAs, int baseTime, int incTime)
	{
		do_login();
		client.send(JoinMatchedMsg.build(gameType, playAs, baseTime, incTime));
	}

	public void getActiveData(String gameId)
	{
		client.send(GetActiveDataMsg.build(gameId));
	}

	public void getArchiveData(String gameId)
	{
		client.send(GetArchiveDataMsg.build(gameId));
	}

	public void joinInvite(String gameId)
	{
		do_login();
		client.send(JoinInviteMsg.build(gameId));
	}

	public void sendMove(String gameId, String moveStr)
	{
		do_login();
		client.send(MakeMoveMsg.build(gameId, moveStr));
	}

	public void resign(String gameId)
	{
		do_login();
		client.send(ResignMsg.build(gameId));
	}

	public void syncGames(SyncType mode, int page)
	{
		if (getUserPass() == null) {
			return;
		}

		switch (mode) {
		case SyncType.ACTIVE:
			if (!hasSynced.getAndSet(true)) {
				do_login();
				client.send(SyncGamesMsg.build(mode, 0));
			}
			break;
		case SyncType.ARCHIVE:
			// TODO: sync archive games
			break;
		}
	}

	public void listenPing(IPingListener listener)
	{
		pingListeners.add(listener);
		var pingMsg = PingMsg.build();
		lastPing.set(pingMsg.time);
		client.send(pingMsg);
	}

	public void unlistenPing(IPingListener listener)
	{
		pingListeners.remove(listener);
	}

	public synchronized void listenMoves(String gameId, RemoteZeroMQPlayer player)
	{
		if (player == null) {
			moveListeners.remove(gameId);
			return;
		}

		var last = moveListeners.put(gameId, player);
		client.tryConnect();

		if (last != player) {
			getActiveData(gameId);
			client.showConnectionError();
		}
	}
}
