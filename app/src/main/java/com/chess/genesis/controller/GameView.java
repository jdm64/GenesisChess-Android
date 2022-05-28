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
import com.chess.genesis.*;
import com.chess.genesis.api.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;

public class GameView implements IGameView2
{
	IGameController2 controller;
	Context ctx;
	BoardView boardView;
	CapturedLayout capturedView;
	PromoteView promoteView;
	PlaceView placeView;

	public GameView(IGameController2 gameCntlr, Context context)
	{
		ctx = context;
		controller = gameCntlr;
		boardView = new BoardView(ctx, null);
		boardView.setController(controller);
		if (Pref.getBool(context, R.array.pf_showCaptured)) {
			capturedView = new CapturedLayout(ctx, null);
			capturedView.setSizes("1/11");
		}
	}

	@Override
	public BoardView getBoardView()
	{
		return boardView;
	}

	@Override
	public CapturedLayout getCapturedView()
	{
		return capturedView;
	}

	@Override
	public PromoteView getPromoteView()
	{
		if (promoteView == null) {
			promoteView = new PromoteView(ctx, null);
			promoteView.setController(controller);
		}
		return promoteView;
	}

	@Override
	public void showPromoteDialog(Move move, int stm)
	{
		getPromoteView().setMove(move, stm);
		controller.showPromoteDialog();
	}

	@Override
	public PlaceView getPlaceView()
	{
		if (placeView == null) {
			placeView = new PlaceView(ctx, null);
			placeView.setController(controller);
		}
		return placeView;
	}

	@Override
	public void showPlaceDialog(int[] counts, int stm)
	{
		getPlaceView().setPieces(counts, stm);
	}

	@Override
	public ISquare getSq(int index)
	{
		return index > 0x88 ? getPlaceSq(index) : getBoardSq(index);
	}

	@Override
	public IBoardSq getBoardSq(int index)
	{
		return boardView.getSquare(index);
	}

	@Override
	public IPlaceSq getPlaceSq(int index)
	{
		return getPlaceView().getPiece(index);
	}

	@Override
	public void setCapturedCounts(int[] counts)
	{
		if (capturedView != null) {
			capturedView.setPieces(counts);
		}
	}
}
