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
package com.chess.genesis.api;

import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;
import androidx.compose.runtime.*;

public interface IGameController
{
	BoardView getBoardView();

	CapturedLayout getCapturedView();

	void onBoardClick(IBoardSq sq);

	void onBoardLongClick(IBoardSq sq);

	void onBackClick();

	void onForwardClick();

	void onCurrentClick();

	void onPlaceClick(ICountSq sq);

	MutableState<Boolean> isGenChess();

	void setBoard(LocalGameEntity data);

	void setBoard(String gameId);

	String getGameId();

	MutableState<Boolean> getPromoteState();

	PromoteView getPromoteView();

	void showPromoteDialog();

	void onPromoteClick(Move move);

	PlaceView getPlaceView();

	void onStmChange(boolean overwrite);

	MutableState<StmState> getStmState();

	void onMove(Move move);

	MutableState<SubmitState> getSubmitState();

	void submitMove(Move move);

	void undoMove();

	void onDispose();
}
