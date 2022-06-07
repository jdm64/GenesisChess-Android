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
	private static GameController INST;

	private Context ctx;
	private IGameModel model;
	private IGameView2 view;
	private IPlayer white;
	private IPlayer black;
	private MutableState<Boolean> isGenState;
	private MutableState<Boolean> promoteState;
	private MutableState<Boolean> placeState;
	private MutableState<Boolean> captureState;
	private MutableState<StmState> stmState;

	public static GameController get(Context context)
	{
		if (INST == null || context != INST.ctx) {
			INST = new GameController(context);
		}
		return INST;
	}

	private GameController(Context context)
	{
		ctx = context;
		view = new GameView(this, ctx);
		promoteState = Util.getState(false);
		placeState = Util.getState(false);
		isGenState = Util.getState(false);
		captureState = Util.getState(Pref.getBool(ctx, R.array.pf_showCaptured));
		stmState = Util.getState(new StmState("White", "Black", 1, 0));
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
		if (model == null || !(isGen ? GenGameModel.class : RegGameModel.class).equals(model.getClass())) {
			model = isGen ? new GenGameModel(view, this) : new RegGameModel(view, this);
		}

		var playingBlack = data.gametype == Enums.REGULAR_CHESS && data.opponent == Enums.CPU_WHITE_OPPONENT;
		var viewAsBlack = Pref.getBool(ctx, R.array.pf_viewAsBlack) && playingBlack;
		view.getBoardView().setViewAsBlack(viewAsBlack);

		model.setBoard(data);
		isGenState.setValue(isGen);
		captureState.setValue(Pref.getBool(ctx, R.array.pf_showCaptured));

		white = data.opponent == Enums.CPU_WHITE_OPPONENT ?
		    new ComputerPlayer(Piece.WHITE, model) : new LocalPlayer(Piece.WHITE, model);
		black = data.opponent == Enums.CPU_BLACK_OPPONENT ?
		    new ComputerPlayer(Piece.BLACK, model) : new LocalPlayer(Piece.BLACK, model);

		onStmChange(true);

		getStmPlayer().takeTurn();
	}

	@Override
	public void setBoard(String gameId)
	{
		Util.runThread(() -> {
			setBoard(LocalGameDao.get(ctx).getGame(gameId));
		});
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
	public MutableState<Boolean> getPlaceState()
	{
		return placeState;
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
	public void onPlaceClick()
	{
		placeState.setValue(true);
		var board = model.getBoard();
		var counts = board.getPieceCounts(Piece.PLACEABLE);
		view.showPlaceDialog(counts, board.getStm());
	}

	@Override
	public void onPlaceClick(IPlaceSq sq)
	{
		if (!getStmPlayer().canClick(model.getBoard().getStm()))
			return;

		model.getMoveHandler().onPlaceClick(sq, model.getBoard().getStm());
		placeState.setValue(false);
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
