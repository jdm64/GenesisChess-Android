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
import com.chess.genesis.R;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;
import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import androidx.compose.runtime.*;

public class GameController implements IGameController2
{
	private final Context ctx;
	private final IGameView2 view;
	private final MutableState<Boolean> isGenState;
	private final MutableState<Boolean> promoteState;
	private final MutableState<Boolean> captureState;
	private final MutableState<StmState> stmState;
	private final MutableState<SubmitState> submitState;

	private IGameModel model;
	private String gameID = "";
	private IPlayer white;
	private IPlayer black;

	public GameController(Context context)
	{
		ctx = context;
		view = new GameView(this, ctx);
		promoteState = Util.getState(false);
		isGenState = Util.getState(false);
		captureState = Util.getState(Pref.getBool(ctx, R.array.pf_showCaptured));
		stmState = Util.getState(new StmState("White", "Black", 1, 0));
		submitState = Util.getState(new SubmitState());
	}

	private IPlayer getStmPlayer()
	{
		return model.getBoard().getStm() == Piece.WHITE ? white : black;
	}

	private IPlayer getNonStmPlayer()
	{
		return model.getBoard().getStm() == Piece.WHITE ? black : white;
	}

	@Override
	public void setBoard(LocalGameEntity data)
	{
		var isGen = data.gametype == Enums.GENESIS_CHESS;
		model = isGen ? new GenGameModel(view, this) : new RegGameModel(view, this);

		view.getBoardView().setViewAsBlack(viewAsBlack(data.gametype, data.opponent));

		model.setBoard(data);
		isGenState.setValue(isGen);
		captureState.setValue(Pref.getBool(ctx, R.array.pf_showCaptured));

		setPlayers(data.opponent);

		onStmChange(true);

		getStmPlayer().takeTurn();
	}

	private boolean viewAsBlack(int gametype, int opponent)
	{
		if (gametype == Enums.GENESIS_CHESS) {
			return false;
		}
		var playingBlack = opponent == Enums.CPU_WHITE_OPPONENT || opponent == Enums.INVITE_WHITE_OPPONENT;
		return playingBlack && Pref.getBool(ctx, R.array.pf_viewAsBlack);
	}

	private void setPlayers(int oppType)
	{
		switch (oppType) {
		default:
		case Enums.HUMAN_OPPONENT:
			white = new LocalPlayer(Piece.WHITE, model);
			black = new LocalPlayer(Piece.BLACK, model);
			return;
		case Enums.CPU_WHITE_OPPONENT:
			white = new ComputerPlayer(Piece.WHITE, model);
			black = new LocalPlayer(Piece.BLACK, model);
			return;
		case Enums.CPU_BLACK_OPPONENT:
			white = new LocalPlayer(Piece.WHITE, model);
			black = new ComputerPlayer(Piece.BLACK, model);
			return;
		case Enums.INVITE_WHITE_OPPONENT:
			white = new RemoteMqttPlayer(Piece.WHITE, model, ctx);
			black = new LocalMqttPlayer(Piece.BLACK, model, submitState);
			return;
		case Enums.INVITE_BLACK_OPPONENT:
			white = new LocalMqttPlayer(Piece.WHITE, model, submitState);
			black = new RemoteMqttPlayer(Piece.BLACK, model, ctx);
			return;
		}
	}

	@Override
	public void setBoard(String gameId)
	{
		gameID = gameId;
		Util.runThread(() -> {
			var game = LocalGameDao.get(ctx).getGame(gameID);
			Util.runUI(() -> setBoard(game));
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
	public MutableState<Boolean> showCapture()
	{
		return captureState;
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
		model.applyMove(move, true);
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
		var state = new StmState(white.getStmName(overwrite), black.getStmName(overwrite), board.getStm(), board.isMate());
		stmState.setValue(state);
	}

	@Override
	public MutableState<StmState> getStmState()
	{
		return stmState;
	}

	@Override
	public void onMove(Move move)
	{
		getNonStmPlayer().finalizeMove(move, ctx);
		getStmPlayer().takeTurn();
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
	public void undoMove()
	{
		model.undoMove();
	}

	@Override
	public void onDispose()
	{
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
	public void loadBoard()
	{
		model.loadBoard();
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
	public void onPlaceClick(IPlaceSq sq)
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
