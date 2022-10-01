/* GenChess, a genesis chess engine
 * Copyright (C) 2015, Justin Madru (justin.jdm64@gmail.com)
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
package com.chess.genesis.api;

import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;

public interface IGameView
{
	BoardView getBoardView();

	CapturedLayout getCapturedView();

	ISquare getSq(int index);

	IBoardSq getBoardSq(int index);

	ICountSq getPlaceSq(int index);

	void setCapturedCounts(int[] counts);

	PromoteView getPromoteView();

	void showPromoteDialog(Move move, int stm);

	PlaceView getPlaceView();

	void setPlaceCounts(int[] counts);
}
