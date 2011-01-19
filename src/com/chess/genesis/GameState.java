package com.chess.genesis;

import android.view.View;

public class GameState
{
	private IntArray callstack;
	private ObjectArray<Move> history;
	private int hindex = -1;

	public GameState()
	{
		callstack = new IntArray();
		history = new ObjectArray<Move>();

	}

	public void pause()
	{
	}

	public void reset()
	{
		hindex = -1;
		callstack.clear();
		history.clear();
		GameActivity.self.board.reset();
		GameActivity.self.gamelayout.setStm();
		GameActivity.self.gamelayout.resetPieces();
	}

	public void backMove()
	{
		if (hindex < 0)
			return;
		Move move = history.get(hindex);
		revertMove(move);
	}

	public void forwardMove()
	{
		if (hindex + 1 >= history.size())
			return;
		Move move = history.get(++hindex);
		applyMove(move, false);
	}
	
	private void handleMove()
	{
		Move move = new Move();

		// create move
		if (callstack.get(0) > 64) {
			move.index = Math.abs(callstack.get(0) - 100);
			move.from = Piece.PLACEABLE;
		} else {
			move.from = callstack.get(0);
		}
		move.to = callstack.get(1);

		// return if move isn't valid
		if (GameActivity.self.board.validMove(move) != Board.VALID_MOVE) {
			callstack.pop();
			return;
		}
		callstack.clear();
		applyMove(move, true);
	}

	private void applyMove(Move move, boolean erase)
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			int king = GameActivity.self.board.kingIndex(GameActivity.self.board.getStm());
			BoardButton kingI = (BoardButton) GameActivity.self.gamelayout.findViewById(king);
			kingI.setCheck(false);
		}

		if (move.from == Piece.PLACEABLE) {
			PlaceButton from = (PlaceButton) GameActivity.self.gamelayout.findViewById(Board.pieceType[move.index] + 100);
			BoardButton to = (BoardButton) GameActivity.self.gamelayout.findViewById(move.to);

			from.setHighlight(false);
			from.minusPiece();
			to.setPiece(from.getPiece());
		} else {
			BoardButton from = (BoardButton) GameActivity.self.gamelayout.findViewById(move.from);
			BoardButton to = (BoardButton) GameActivity.self.gamelayout.findViewById(move.to);

			to.setPiece(from.getPiece());
			from.setPiece(0);
			from.setHighlight(false);
		}

		// apply move to board
		GameActivity.self.board.make(move);
		// update hindex, history
		if (erase) {
			hindex++;
			if (hindex < history.size())
				history.resize(hindex);
			history.push(move);
		}

		// move caused check
		if (GameActivity.self.board.incheck(GameActivity.self.board.getStm())) {
			int king = GameActivity.self.board.kingIndex(GameActivity.self.board.getStm());
			BoardButton kingI = (BoardButton) GameActivity.self.gamelayout.findViewById(king);
			kingI.setCheck(true);
		}

		GameActivity.self.gamelayout.setStm();
	}

	private void revertMove(Move move)
	{
		// legal move always ends with king not in check
		if (hindex > 1) {
			int king = GameActivity.self.board.kingIndex(GameActivity.self.board.getStm());
			BoardButton kingI = (BoardButton) GameActivity.self.gamelayout.findViewById(king);
			kingI.setCheck(false);
		}

		if (move.from == Piece.PLACEABLE) {
			PlaceButton from = (PlaceButton) GameActivity.self.gamelayout.findViewById(Board.pieceType[move.index] + 100);
			BoardButton to = (BoardButton) GameActivity.self.gamelayout.findViewById(move.to);

			to.setPiece(0);
			from.plusPiece();
		} else {
			BoardButton from = (BoardButton) GameActivity.self.gamelayout.findViewById(move.from);
			BoardButton to = (BoardButton) GameActivity.self.gamelayout.findViewById(move.to);

			from.setPiece(to.getPiece());

			if (move.xindex == Piece.NONE)
				to.setPiece(0);
			else
				to.setPiece(Board.pieceType[move.xindex]);
		}
		hindex--;
		GameActivity.self.board.unmake(move);

		// move caused check
		if (GameActivity.self.board.incheck(GameActivity.self.board.getStm())) {
			int king = GameActivity.self.board.kingIndex(GameActivity.self.board.getStm());
			BoardButton kingI = (BoardButton) GameActivity.self.gamelayout.findViewById(king);
			kingI.setCheck(true);
		}

		GameActivity.self.gamelayout.setStm();
	}

	public void boardClick(View v)
	{
		BoardButton to = (BoardButton) v;
		int index = to.getIndex();

		if (callstack.size() == 0) {
		// No active clicks
			// first click must be non empty and your own
			if (to.getPiece() == 0 || to.getPiece() * GameActivity.self.board.getStm() < 0)
				return;
			callstack.push(index);
			to.setHighlight(true);
			return;
		} else if (callstack.get(0) == index) {
		// clicking the same square
			callstack.clear();
			to.setHighlight(false);
			return;
		} else if (callstack.get(0) > 64) {
		// Place piece action
			// can't place on another piece
			if (to.getPiece() != 0)
				return;
		} else {
		// piece move action
			BoardButton from = (BoardButton) GameActivity.self.gamelayout.findViewById(callstack.get(0));
			// capturing your own piece
			if (from.getPiece() * to.getPiece() > 0)
				return;
		}
		callstack.push(index);
		handleMove();
	}

	public void placeClick(View v)
	{
		PlaceButton from = (PlaceButton) v;
		int type = from.getPiece();

		// only select your own pieces where count > 0
		if (type * GameActivity.self.board.getStm() < 0 || from.getCount() <= 0)
			return;
		if (callstack.size() == 0) {
		// No active clicks
			callstack.push(type + 100);
		} else if (callstack.get(0) < 64) {
		// switching from board to place piece
			BoardButton to = (BoardButton) GameActivity.self.gamelayout.findViewById(callstack.get(0));
			to.setHighlight(false);
			callstack.set(0, type + 100);
		} else if (callstack.get(0) == type + 100) {
		// clicking the same square
			callstack.clear();
			from.setHighlight(false);
			return;
		} else {
		// switching to another place piece
			PlaceButton fromold = (PlaceButton) GameActivity.self.gamelayout.findViewById(callstack.get(0));
			fromold.setHighlight(false);
			callstack.set(0, type + 100);
			from.setHighlight(true);
			return;
		}
		from.setHighlight(true);
		GameActivity.self.gamelayout.placeButtonClick();
	}
}
