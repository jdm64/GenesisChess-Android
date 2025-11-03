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

public interface IGameModel
{
	int PLACEOFFSET = 1000;

	Board getBoard();

	ObjectArray<Move> getHistory();

	IMoveHandler getMoveHandler();

	void reset();

	void setBoard(GameEntity gameData);

	GameEntity saveBoard();

	void loadBoard();

	boolean isCurrentMove();

	void backMove();

	void forwardMove();

	void currentMove();

	void handleMove(int from, int to);

	/**
	 * @param time positive time overwrites history
	 */
	void applyMove(Move move, long time);

	void revertMove(Move move);

	void undoMove();
}
