/*	GenesisChess, an Android chess application
	Copyright (C) 2012, Justin Madru (justin.jdm64@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.chess.genesis.engine;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import com.chess.genesis.activity.*;
import com.chess.genesis.data.*;
import com.chess.genesis.net.*;
import com.chess.genesis.view.*;

public class GenGameState extends GameState
{
	private final Handler xhandle = new Handler()
	{
		@Override
		public void handleMessage(final Message msg)
		{
			handleOther(msg);
		}
	};

	public GenGameState(final Activity _activity, final GameFrag _game, final Bundle _settings)
	{
		super(_activity, _game, _settings);
		handle = xhandle;
		moveType = new GenMove();
		board = new GenBoard();

		switch (type) {
		case Enums.LOCAL_GAME:
		default:
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
			cpu = new GenEngine(handle);
			cpu.setTime(pref.getInt(PrefKey.CPUTIME, cpu.getTime()));
			oppType = Integer.parseInt(settings.getString("opponent"));
			net = null;
			ycol = (oppType == Enums.CPU_WHITE_OPPONENT)? Piece.BLACK : Piece.WHITE;
			break;
		case Enums.ONLINE_GAME:
		case Enums.ARCHIVE_GAME:
			oppType = Enums.HUMAN_OPPONENT;
			cpu = null;
			net = new NetworkClient(activity, handle);
			ycol = settings.getString("username").equals(settings.getString("white"))? Piece.WHITE : Piece.BLACK;
			break;
		}

		final String tmp = settings.getString("history");
		if (tmp == null || tmp.length() < 3) {
			check_endgame();
			return;
		}
		final String[] movehistory = tmp.trim().split(" +");

		for (final String element : movehistory) {
			final GenMove move = new GenMove();
			move.parse(element);

			if (board.validMove(move) != Move.VALID_MOVE)
				break;
			history.push(move);
			board.make(move);
			hindex++;
		}
		check_endgame();
	}

	@Override
	public void setBoard()
	{
		// set place piece counts
		setBoard(board.getPieceCounts(Piece.PLACEABLE));
	}

	private void handleMove()
	{
		if (type == Enums.ONLINE_GAME) {
			// you can't edit the past in online games
			if (hindex + 1 < history.size()) {
				callstack.pop();
				return;
			}
		} else if (type == Enums.ARCHIVE_GAME) {
			return;
		}

		final GenMove move = new GenMove();

		// create move
		if (callstack.get(0) > 0x88) {
			move.index = Math.abs(callstack.get(0) - 1000);
			move.from = Piece.PLACEABLE;
		} else {
			move.from = callstack.get(0);
		}
		move.to = callstack.get(1);

		// return if move isn't valid
		if (board.validMove(move) != Move.VALID_MOVE) {
			callstack.pop();
			return;
		}
		callstack.clear();
		applyMove(move, true, true);
	}

	@Override
	protected void applyMove(final Move move, final boolean erase, final boolean localmove)
	{
		preApplyMove();

		if (move.from == Piece.PLACEABLE) {
			final PlaceButton from = (PlaceButton) activity.findViewById(Move.InitPieceType[move.index] + 1000);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			from.setHighlight(false);
			from.minusCount();
			to.setPiece(from.getPiece());
			to.setLast(true);
		} else {
			final BoardButton from = (BoardButton) activity.findViewById(move.from);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			to.setPiece(from.getPiece());
			to.setLast(true);
			from.setPiece(0);
			from.setHighlight(false);
		}

		// apply move to board
		board.make(move);

		// update hindex, history
		hindex++;
		if (erase) {
			if (hindex < history.size())
				history.resize(hindex);
			history.push(move);
			if (localmove)
				save(activity, false);
		}
		postApplyMove();
	}

	@Override
	protected void revertMove(final Move move)
	{
		preRevertMove();

		if (move.from == Piece.PLACEABLE) {
			final PlaceButton from = (PlaceButton) activity.findViewById(Move.InitPieceType[move.index] + 1000);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			to.setLast(false);
			to.setPiece(0);
			from.plusCount();
		} else {
			final BoardButton from = (BoardButton) activity.findViewById(move.from);
			final BoardButton to = (BoardButton) activity.findViewById(move.to);

			from.setPiece(to.getPiece());

			to.setLast(false);
			if (move.xindex == Piece.NONE)
				to.setPiece(0);
			else
				to.setPiece(Move.InitPieceType[move.xindex]);
		}

		board.unmake(move);
		hindex--;

		postRevertMove();
	}

	@Override
	public void boardClick(final View v)
	{
		final BoardButton to = (BoardButton) v;
		final int index = to.getIndex();
		final int col = yourColor();

		if (callstack.size() == 0) {
		// No active clicks
			// first click must be non empty and your own
			if (to.getPiece() * col <= 0)
				return;
			callstack.push(index);
			to.setHighlight(true);
			return;
		} else if (callstack.get(0) == index) {
		// clicking the same square
			callstack.clear();
			to.setHighlight(false);
			return;
		} else if (callstack.get(0) > 0x88) {
		// Place piece action
			// can't place on another piece
			if (to.getPiece() * col < 0) {
				return;
			} else if (to.getPiece() * col > 0) {
				final PlaceButton from = (PlaceButton) activity.findViewById(callstack.get(0));
				from.setHighlight(false);
				to.setHighlight(true);
				callstack.set(0, index);
				return;
			}
		} else {
		// piece move action
			final BoardButton from = (BoardButton) activity.findViewById(callstack.get(0));
			// capturing your own piece (switch to piece)
			if (from.getPiece() * to.getPiece() > 0) {
				from.setHighlight(false);
				to.setHighlight(true);
				callstack.set(0, index);
				return;
			}
		}
		callstack.push(index);
		handleMove();
	}

	@Override
	public void placeClick(final View v)
	{
		final PlaceButton from = (PlaceButton) v;
		final int col = yourColor();
		final int ptype = from.getPiece();

		// only select your own pieces where count > 0
		if (board.getStm() != col || ptype * board.getStm() < 0 || from.getCount() <= 0)
			return;
		if (callstack.size() == 0) {
		// No active clicks
			callstack.push(ptype + 1000);
			from.setHighlight(true);
		} else if (callstack.get(0) < 0x88) {
		// switching from board to place piece
			final BoardButton to = (BoardButton) activity.findViewById(callstack.get(0));
			to.setHighlight(false);
			callstack.set(0, ptype + 1000);
			from.setHighlight(true);
		} else if (callstack.get(0) == ptype + 1000) {
		// clicking the same square
			callstack.clear();
			from.setHighlight(false);
		} else {
		// switching to another place piece
			final PlaceButton fromold = (PlaceButton) activity.findViewById(callstack.get(0));
			fromold.setHighlight(false);
			callstack.set(0, ptype + 1000);
			from.setHighlight(true);
		}
		AnimationFactory.flipTransition(game.game_board);
	}
}
