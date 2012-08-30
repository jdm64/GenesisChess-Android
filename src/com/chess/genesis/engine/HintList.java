package com.chess.genesis.engine;

import android.app.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;

public class HintList
{
	protected final Activity activity;
	protected final GameState gamestate;
	protected final Board board;
	protected final IntArray hints;
	protected int selected;
	protected SelectType type;

	enum SelectType
	{
		NONE, SELECT, PIECE_MOVES, MOVES_TO
	}

	public HintList(final Activity _activity, final GameState _gameState, final Board _board)
	{
		gamestate = _gameState;
		activity = _activity;
		board = _board;
		hints = new IntArray();
		selected = Piece.NONE;
		type = SelectType.NONE;
	}

	public int getSelected()
	{
		return selected;
	}

	public void clearSelect()
	{
		if (type == SelectType.NONE)
			return;
		final PieceImg piece = (PieceImg) activity.findViewById(selected);
		piece.setHighlight(false);
		selected = Piece.NONE;
		type = SelectType.NONE;
	}

	public void clearHint()
	{
		for (int i = 0; i < hints.size(); i++) {
			final BoardButton button = (BoardButton) activity.findViewById(hints.get(i));
			button.setHighlight(false);
		}
		hints.clear();
	}

	public void clear()
	{
		clearSelect();
		clearHint();
		return;
	}

	private void setSelected(final PieceImg bb, final int index)
	{
		type = SelectType.SELECT;
		selected = index;
		bb.setHighlight(true);
	}

	public void boardClick(final BoardButton bb, final int ycolor)
	{
		final int index = bb.getIndex();
		final int piece = bb.getPiece();

		if (selected == Piece.NONE) {
		// No active clicks
			// first click must be non empty and your own
			if (piece * ycolor <= 0)
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

	public void longBoardClick(final BoardButton bb, final int ycolor)
	{
		final int index = bb.getIndex();
		final boolean isYourPiece = (bb.getPiece() * ycolor > 0);

		clear();
		setSelected(bb, index);
		if (isYourPiece)
			showPieceMoves(index);
		else
			showMovesTo(index);
	}

	public void placeClick(final PlaceButton pb, final int ycolor)
	{
		final int index = pb.getId();
		final int piece = pb.getPiece();

		// only select your own pieces where count > 0
		if (piece * ycolor < 0 || pb.getCount() <= 0)
			return;
		if (selected == Piece.NONE) {
			// No active clicks
			setSelected(pb, index);
		} else if (selected == index) {
			// clicking the same square
			clearSelect();
		} else if (selected < 0x88) {
			// switching from board to place piece
			clear();
			setSelected(pb, index);
		} else {
			// switching to another place piece
			clear();
			setSelected(pb, index);
		}
		AnimationFactory.flipTransition(gamestate.game.game_board);
	}

	public void showPieceMoves(final int square)
	{
		type = SelectType.PIECE_MOVES;
		selected = square;
		final MoveList moveList = board.getMoveList(board.getStm(), Move.MOVE_ALL);
		for (final MoveNode node : moveList.list) {
			if (node.move.from == selected) {
				hints.push(node.move.to);
				final BoardButton button = (BoardButton) activity.findViewById(hints.top());
				button.setHighlight(true);
			}
		}
		board.getMoveListPool().put(moveList);
	}

	public void showMovesTo(final int square)
	{
		type = SelectType.MOVES_TO;
		selected = square;
		final MoveList moveList = board.getMoveList(board.getStm(), Move.MOVE_ALL);
		for (final MoveNode node : moveList.list) {
			if (node.move.to == selected && node.move.from != Piece.PLACEABLE) {
				hints.push(node.move.from);
				final BoardButton button = (BoardButton) activity.findViewById(hints.top());
				button.setHighlight(true);
			}
		}
		board.getMoveListPool().put(moveList);
	}
}
