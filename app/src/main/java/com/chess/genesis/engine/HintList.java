/* GenChess, a genesis chess engine
 * Copyright (C) 2014, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.engine;

import java.util.*;
import com.chess.genesis.api.*;

public class HintList implements IMoveHandler
{
	private final IGameFrag gamefrag;
	private final GameState gamestate;
	private final Board board;
	private final List<Integer> hints;
	private int selected;
	private SelectType type;

	enum SelectType
	{
		NONE, SELECT, PIECE_MOVES, MOVES_TO
	}

	public HintList(final IGameFrag gameFrag, final GameState _gameState, final Board _board)
	{
		gamestate = _gameState;
		gamefrag = gameFrag;
		board = _board;
		hints = new ArrayList<>();
		selected = Piece.NONE;
		type = SelectType.NONE;
	}

	public int getSelected()
	{
		return selected;
	}

	private void clearSelect()
	{
		if (type == SelectType.NONE)
			return;
		final ISquare piece = gamefrag.getSq(selected);
		piece.setHighlight(false);
		selected = Piece.NONE;
		type = SelectType.NONE;
	}

	private void clearHint()
	{
		for (final Integer i : hints) {
			final IBoardSq button = gamefrag.getBoardSq(i);
			button.setHighlight(false);
		}
		hints.clear();
	}

	public void clear()
	{
		clearSelect();
		clearHint();
	}

	private void setSelected(final ISquare bb, final int index)
	{
		type = SelectType.SELECT;
		selected = index;
		bb.setHighlight(true);
	}

	@Override
	public void onBoardClick(IBoardSq bb, int ycolor)
	{
		final int index = bb.getIndex();
		final int piece = bb.getPiece();

		if (selected == Piece.NONE) {
		// No active clicks
			// first click must be your own on your turn
			if (board.getStm() != ycolor || piece * ycolor < 0)
				return;
			setSelected(bb, index);
			return;
		} else if (selected == index) {
			clear();
			return;
		} else if (selected > 0x88) {
		// Place piece action
			// can't place on another piece
			if (piece * ycolor < 0) {
				return;
			} else if (piece * ycolor > 0) {
				clearSelect();
				setSelected(bb, index);
				return;
			}
		} else if (piece * ycolor > 0) {
			// capturing your own piece (switch to piece)
			if (type != SelectType.MOVES_TO || !hints.contains(index)) {
				clear();
				setSelected(bb, index);
				return;
			}
		}

		if (type == SelectType.MOVES_TO)
			gamestate.handleMove(index, selected);
		else
			gamestate.handleMove(selected, index);
	}

	@Override
	public void onBoardLongClick(IBoardSq bb, int ycolor)
	{
		// only allow long click on your turn
		if (board.getStm() != ycolor)
			return;

		final int index = bb.getIndex();
		final boolean isYourPiece = (bb.getPiece() * ycolor > 0);

		clear();
		setSelected(bb, index);
		if (isYourPiece)
			showPieceMoves(index);
		else
			showMovesTo(index);
	}

	@Override
	public void onPlaceClick(IPlaceSq pb, int ycolor)
	{
		final int index = pb.getIndex();
		final int piece = pb.getPiece();

		// only select your own pieces on your turn where count > 0
		if (board.getStm() != ycolor || piece * ycolor < 0 || pb.getCount() <= 0)
			return;
		if (selected == Piece.NONE) {
			// No active clicks
			setSelected(pb, index);
		} else if (selected == index) {
			// clicking the same square
			clearSelect();
		} else {
			// switching from board to place piece
			// OR
			// switching to another place piece
			clear();
			setSelected(pb, index);
		}

		gamefrag.togglePlaceBoard();
	}

	private void showPieceMoves(final int square)
	{
		type = SelectType.PIECE_MOVES;
		selected = square;
		final MoveList moveList = board.getMoveList(board.getStm(), Move.MOVE_ALL);
		for (final MoveNode node : moveList) {
			if (node.move.from == selected) {
				hints.add(node.move.to);
				final IBoardSq button = gamefrag.getBoardSq(node.move.to);
				button.setHighlight(true);
			}
		}
		board.getMoveListPool().put(moveList);
	}

	private void showMovesTo(final int square)
	{
		type = SelectType.MOVES_TO;
		selected = square;
		final MoveList moveList = board.getMoveList(board.getStm(), Move.MOVE_ALL);
		for (final MoveNode node : moveList) {
			if (node.move.to == selected && node.move.from != Piece.PLACEABLE) {
				hints.add(node.move.from);
				final IBoardSq button = gamefrag.getBoardSq(node.move.from);
				button.setHighlight(true);
			}
		}
		board.getMoveListPool().put(moveList);
	}
}
