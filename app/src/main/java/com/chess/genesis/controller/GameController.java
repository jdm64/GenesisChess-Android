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

import java.lang.ref.*;
import java.util.concurrent.atomic.*;
import android.content.*;
import com.chess.genesis.R;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;
import com.chess.genesis.data.Enums.*;
import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.net.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import androidx.compose.runtime.*;

public class GameController implements IGameController
{
	private final Context ctx;
	private final IGameView view;
	private final MutableState<Boolean> isGenState;
	private final MutableState<Boolean> promoteState;
	private final MutableState<SubmitState> submitState;
	private final MutableState<Boolean> resignState;

	private IGameModel model;
	private String gameID = "";
	private IPlayer white;
	private IPlayer black;
	private int yourColor = 0;

	public GameController(Context context, GameSource source, String gameID)
	{
		ctx = context;
		view = new GameView(this, ctx);
		promoteState = Util.getState(false);
		isGenState = Util.getState(false);
		submitState = Util.getState(new SubmitState());
		resignState = Util.getState(false);

		// init default values so no NPE in onDestroy()
		model = new GenGameModel(view, this);
		white = new LocalPlayer(Piece.WHITE, model);
		black = new LocalPlayer(Piece.BLACK, model);

		setBoard(source, gameID);
	}

	private IPlayer getStmPlayer()
	{
		return model.getBoard().getStm() == Piece.WHITE ? white : black;
	}

	private IPlayer getNonStmPlayer()
	{
		return model.getBoard().getStm() == Piece.WHITE ? black : white;
	}

	private IPlayer getYourPlayer()
	{
		return switch (yourColor) {
			case Piece.WHITE -> white;
			case Piece.BLACK -> black;
			default -> null;
		};
	}

	@Override
	public void setBoard(GameEntity data)
	{
		Util.setScreenOnFlag(ctx, true);
		new PrefEdit(ctx).putString(R.array.pf_lastpage, "board/" + data.getSource().name + "/" + data.gameid).commit();

		var isGen = data.gametype == GameType.GENESIS.id;
		model = isGen ? new GenGameModel(view, this) : new RegGameModel(view, this);
		yourColor = Enums.from(OpponentType.class, data.opponent).yourColor;
		view.getBoardView().setViewAsBlack(viewAsBlack(data.gametype, data.opponent));

		model.setBoard(data);
		isGenState.setValue(isGen);

		setPlayers(data.opponent);

		onStmChange(false);
		getStmView().setClockState(model.updateClock());
		view.setRatings(data.getRatings());
		getStmPlayer().takeTurn(ctx);
	}

	private boolean viewAsBlack(int gametype, int opponent)
	{
		if (gametype == GameType.GENESIS.id) {
			return false;
		}
		var playingBlack = opponent == OpponentType.CPU_WHITE.id || opponent == OpponentType.REMOTE_WHITE.id;
		return playingBlack && Pref.getBool(ctx, R.array.pf_viewAsBlack);
	}

	private void setPlayers(int oppType)
	{
		switch (Enums.from(OpponentType.class, oppType)) {
		case OpponentType.CPU_WHITE:
			white = new ComputerPlayer(Piece.WHITE, model);
			black = new LocalPlayer(Piece.BLACK, model);
			return;
		case OpponentType.CPU_BLACK:
			white = new LocalPlayer(Piece.WHITE, model);
			black = new ComputerPlayer(Piece.BLACK, model);
			return;
		case OpponentType.REMOTE_WHITE:
			white = new RemoteZeroMQPlayer(Piece.WHITE, model, ctx);
			black = new LocalZeroMQPlayer(Piece.BLACK, model, submitState);
			return;
		case OpponentType.REMOTE_BLACK:
			white = new LocalZeroMQPlayer(Piece.WHITE, model, submitState);
			black = new RemoteZeroMQPlayer(Piece.BLACK, model, ctx);
			return;
		case OpponentType.ARCHIVED:
			white = new ArchivedPlayer(Piece.WHITE, model);
			black = new ArchivedPlayer(Piece.BLACK, model);
			return;
		case OpponentType.HUMAN:
		default:
			white = new LocalPlayer(Piece.WHITE, model);
			black = new LocalPlayer(Piece.BLACK, model);
		}
	}

	@Override
	public GameSource getSource()
	{
		var entity = model.getGameEntity();
		return entity != null ? entity.getSource() : null;
	}

	@Override
	public void setBoard(GameSource source, String gameId)
	{
		gameID = gameId;
		Util.runThread(() -> {
			GameEntity game = source == GameSource.ARCHIVE
				? ArchiveGameDao.get(ctx).getGame(gameID)
				: ActiveGameDao.get(ctx).getGame(gameID);

			if (game instanceof ActiveGameEntity actGame && actGame.hasArchiveData) {
				var archGame = ArchiveGameDao.get(ctx).getGame(gameID);
				if (archGame != null) {
					ActiveGameDao.get(ctx).deleteGame(gameID);
					game = archGame;
				}
			}

			if (game != null) {
				var ref = new WeakReference<>(game);
				Util.runUI(() -> setBoard(ref.get()));
			}
		});
	}

	@Override
	public void reloadAsArchived()
	{
		Util.runThread(() -> {
			var game = ArchiveGameDao.get(ctx).getGame(gameID);
			if (game != null) {
				ActiveGameDao.get(ctx).deleteGame(gameID);
				Util.runUI(() -> setBoard(game));
			}
		});
	}

	@Override
	public String getGameId()
	{
		return gameID;
	}

	@Override
	public MutableState<Boolean> isGenChess()
	{
		return isGenState;
	}

	@Override
	public StmView getStmView()
	{
		return view.getStmView();
	}

	@Override
	public BoardView getBoardView()
	{
		return view.getBoardView();
	}

	@Override
	public CapturedLayout getCapturedView()
	{
		return view.getCapturedView();
	}

	@Override
	public PromoteView getPromoteView()
	{
		return view.getPromoteView();
	}

	@Override
	public void onPromoteClick(Move move)
	{
		model.applyMove(move, System.currentTimeMillis());
		promoteState.setValue(false);
	}

	@Override
	public PlaceView getPlaceView()
	{
		return view.getPlaceView();
	}

	@Override
	public void onStmChange(boolean overwrite)
	{
		var board = model.getBoard();
		var boardMate = board.isMate();
		var gameStatus = switch (boardMate) {
			case Board.NOT_MATE -> GameStatus.ACTIVE;
			case Board.CHECK_MATE -> board.getStm() == Piece.WHITE ? GameStatus.WHITE_MATE : GameStatus.BLACK_MATE;
			case Board.STALE_MATE -> GameStatus.STALEMATE;
			default -> GameStatus.ACTIVE;
		};
		var stmState = new StmState(white.getStmName(overwrite), black.getStmName(overwrite), board.getStm(), gameStatus, yourColor);
		getStmView().setStmState(stmState);
	}

	@Override
	public void onMove(Move move)
	{
		getStmView().setClockState(model.updateClock());

		getNonStmPlayer().finalizeMove(move, ctx);
		getStmPlayer().takeTurn(ctx);
	}

	@Override
	public MutableState<SubmitState> getSubmitState()
	{
		return submitState;
	}

	@Override
	public void submitMove(Move move)
	{
		getNonStmPlayer().submitMove(move, ctx);
	}

	@Override
	public void resign()
	{
		var player = getYourPlayer();
		if (player != null) {
			player.resign(ctx);
		}
	}

	@Override
	public void onClockTimeout()
	{
		var oppType = Enums.from(OpponentType.class, model.getGameEntity().opponent);
		if (oppType == OpponentType.REMOTE_WHITE || oppType == OpponentType.REMOTE_BLACK) {
			ZeroMQClient.bind(ctx, handler -> handler.getActiveData(gameID));
		}
	}

	@Override
	public void undoMove()
	{
		model.undoMove();
	}

	@Override
	public void onResume()
	{
		white.onResume(ctx);
		black.onResume(ctx);
	}

	@Override
	public void onDispose()
	{
		Util.setScreenOnFlag(ctx, false);
		white.onDispose(ctx);
		black.onDispose(ctx);
	}

	@Override
	public void showPromoteDialog()
	{
		promoteState.setValue(true);
	}

	@Override
	public MutableState<Boolean> getPromoteState()
	{
		return promoteState;
	}

	@Override
	public MutableState<Boolean> getResignState()
	{
		return resignState;
	}

	@Override
	public void onBoardClick(IBoardSq sq)
	{
		if (!getStmPlayer().canClick(model.getBoard().getStm()))
			return;

		model.getMoveHandler().onBoardClick(sq, model.getBoard().getStm());
	}

	@Override
	public void onBoardLongClick(IBoardSq sq)
	{
		if (!getStmPlayer().canClick(model.getBoard().getStm()))
			return;

		model.getMoveHandler().onBoardLongClick(sq, model.getBoard().getStm());
	}

	@Override
	public void onPlaceClick(ICountSq sq)
	{
		if (!getStmPlayer().canClick(model.getBoard().getStm()))
			return;

		model.getMoveHandler().onPlaceClick(sq, model.getBoard().getStm());
	}

	@Override
	public void onBackClick()
	{
		model.backMove();
	}

	@Override
	public void onForwardClick()
	{
		model.forwardMove();
	}

	@Override
	public void onCurrentClick()
	{
		model.currentMove();
	}
}
