/* GenChess, a genesis chess engine
 * Copyright (C) 2022, Justin Madru (justin.jdm64@gmail.com)
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
package com.chess.genesis.controller;

import com.chess.genesis.api.*;
import com.chess.genesis.db.*;
import com.chess.genesis.engine.*;

public abstract class GameModel implements IGameModel
{
	protected final Board board;
	protected final ObjectArray<MoveFlags> flagsHistory = new ObjectArray<>(MoveFlags::new);
	protected final IMoveHandler moveHandler;
	protected final TimedObjectArr<Move> history = new TimedObjectArr<>(Move::new);
	protected final IGameView view;
	protected final IGameController controller;

	protected GameEntity data;
	protected int hindex = -1;

	public GameModel(Board _board, IGameView _view, IGameController _controller)
	{
		controller = _controller;
		view = _view;
		board = _board;
		moveHandler = new MoveHandler(this, view);
	}

	@Override
	public Board getBoard()
	{
		return board;
	}

	@Override
	public ObjectArray<Move> getHistory()
	{
		return history;
	}

	@Override
	public IMoveHandler getMoveHandler()
	{
		return moveHandler;
	}

	@Override
	public void reset()
	{
		hindex = -1;
		history.clear();
		board.reset();
		moveHandler.clear();
		flagsHistory.clear();
	}

	/**
	 * for adding moved to history from data from setBoard()
	 */
	protected void addMove(Move move, long time)
	{
		var flags = new MoveFlags();
		board.getMoveFlags(flags);
		flagsHistory.push(flags);

		hindex++;
		board.make(move);
		history.pushWithTime(move, time);
	}

	@Override
	public void setBoard(GameEntity _data)
	{
		data = _data;
		reset();

		var movehistory = data.history.trim().split(" +");
		for (var element : movehistory) {
			var parts = element.split(",");
			var res = board.parseMove(parts[0]);
			if (res.second != Board.VALID_MOVE)
				break;
			addMove(res.first, Long.parseLong(parts[1]));
		}
		loadBoard();
	}

	@Override
	public void loadBoard()
	{
		// set board pieces
		var squares = board.getBoardArray();
		for (int i = 0; i < 64; i++) {
			var loc = Board.SF88(i);
			var button = view.getBoardSq(loc);
			button.reset();
			button.setPiece(squares[loc]);
		}
		// set last move highlight
		if (history.size() != 0) {
			var to = view.getBoardSq(history.top().to);
			to.setLast(true);
		}

		// move caused check
		if (board.inCheck(board.getStm())) {
			var king = board.kingIndex(board.getStm());
			var kingI = view.getBoardSq(king);
			kingI.setCheck(true);
		}
		// set captured pieces
		view.setCapturedCounts(board.getPieceCounts(Piece.DEAD));
	}

	@Override
	public GameEntity saveBoard()
	{
		data.history = history.toString();
		data.zfen = board.printZFen();
		data.stime = System.currentTimeMillis();
		return data;
	}

	@Override
	public boolean isCurrentMove()
	{
		return hindex + 1 >= history.size();
	}

	@Override
	public void undoMove()
	{
		if (hindex < 0)
			return;
		revertMove(history.pop());
	}

	@Override
	public void backMove()
	{
		if (hindex < 0)
			return;
		revertMove(history.get(hindex));
	}

	@Override
	public void forwardMove()
	{
		if (isCurrentMove())
			return;
		applyMove(history.get(hindex + 1), false);
	}

	@Override
	public void currentMove()
	{
		var len = history.size();
		while (hindex + 1 < len)
			applyMove(history.get(hindex + 1), false);
	}

	void preCommonMove()
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			var king = board.kingIndex(board.getStm());
			var kingI = view.getBoardSq(king);
			kingI.setCheck(false);
		}
	}

	void preApplyMove()
	{
		moveHandler.clear();
		if (hindex >= 0) {
			// undo last move highlight
			var to = view.getBoardSq(history.get(hindex).to);
			to.setLast(false);

			preCommonMove();
		}
	}

	void preRevertMove()
	{
		moveHandler.clear();
		preCommonMove();
	}

	void postCommonMove(boolean overwrite)
	{
		// move caused check
		if (board.inCheck(board.getStm())) {
			var king = board.kingIndex(board.getStm());
			var kingI = view.getBoardSq(king);
			kingI.setCheck(true);
		}
		// set captured pieces
		view.setCapturedCounts(board.getPieceCounts(Piece.DEAD));

		controller.onStmChange(overwrite);
	}

	void postRevertMove()
	{
		// redo last move highlight
		if (hindex >= 0) {
			var hto = view.getBoardSq(history.get(hindex).to);
			hto.setLast(true);
		}
		postCommonMove(false);
	}
}
