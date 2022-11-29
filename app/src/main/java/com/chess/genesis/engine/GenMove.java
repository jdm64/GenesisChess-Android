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

import android.os.*;
import androidx.annotation.*;

public class GenMove extends Move
{
	public static final Parcelable.Creator<GenMove> CREATOR = new Parcelable.Creator<>()
	{
		@Override
		public GenMove createFromParcel(final Parcel in)
		{
			return new GenMove(in);
		}

		@Override
		public GenMove[] newArray(final int size)
		{
			return new GenMove[size];
		}
	};

	public GenMove()
	{
	}

	private GenMove(final Parcel in)
	{
		super(in);
	}

	@Override
	public GenMove get()
	{
		return new GenMove();
	}

	@Override
	protected StringBuilder printLoc(final int loc)
	{
		final StringBuilder str = new StringBuilder();

		if (loc > Piece.PLACEABLE) {
			str.append(printSq(loc));
		} else if (loc == Piece.PLACEABLE) {
			str.append("aval");
		} else {
			str.append("dead");
		}
		return str;
	}

	@NonNull
	@Override
	public String toString()
	{
		final StringBuilder out = new StringBuilder();

		if (from == Piece.PLACEABLE)
			out.append(PIECE_SYM[Math.abs(Board.InitPieceType[index]) + 6]);
		else
			out.append(printLoc(from));
		out.append(printLoc(to));
		return out.toString();
	}

	@Override
	public boolean parse(final String str)
	{
		if (str == null || str.isEmpty())
			return false;
		final char[] s = str.toCharArray();
		boolean place = true;

		switch (s[0]) {
		case 'a':	case 'b':
		case 'c':	case 'd':
		case 'e':	case 'f':
		case 'g':	case 'h':
			place = false;
			index = Piece.EMPTY;
			break;
		case 'P':
			index = Piece.PAWN;
			break;
		case 'N':
			index = Piece.KNIGHT;
			break;
		case 'B':
			index = Piece.BISHOP;
			break;
		case 'R':
			index = Piece.ROOK;
			break;
		case 'Q':
			index = Piece.QUEEN;
			break;
		case 'K':
			index = Piece.KING;
			break;
		default:
			return false;
		}
		if (place) {
			// parse placement move
			if (s[1] < 'a' || s[1] > 'h' || s[2] < '0' || s[2] > '9')
				return false;
			to = 16 * (s[2] - '1') + (s[1] - 'a');
			from = Piece.PLACEABLE;
		} else {
			// parse movement move
			if (s[1] < '0' || s[1] > '9' || s[2] < 'a' || s[2] > 'h' || s[3] < '0' || s[3] > '9')
				return false;
			from = 16 * (s[1] - '1') + (s[0] - 'a');
			to = 16 * (s[3] - '1') + (s[2] - 'a');
		}
		return true;
	}
}
