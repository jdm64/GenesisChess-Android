/*	GenChess, a genesis chess engine
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

class DistDB
{
	public final static DistDB[] TABLE = {
		new DistDB( 0,  Piece.EMPTY), new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK),
		new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK),
		new DistDB( 1,   Piece.ROOK), new DistDB( 1,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(14, Piece.KNIGHT),
		new DistDB(15, Piece.BISHOP), new DistDB(16,   Piece.ROOK), new DistDB(17, Piece.BISHOP),
		new DistDB(18, Piece.KNIGHT), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB(31, Piece.KNIGHT), new DistDB(16,   Piece.ROOK),
		new DistDB(33, Piece.KNIGHT), new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(17, Piece.BISHOP),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(16,   Piece.ROOK),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(17, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB(15, Piece.BISHOP), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB(16,   Piece.ROOK), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY),
		new DistDB( 0,  Piece.EMPTY), new DistDB( 0,  Piece.EMPTY), new DistDB(17, Piece.BISHOP) };

	public DistDB(final int Step, final int Type)
	{
		step = Step;
		type = Type;
	}

	public int step;
	public int type;
}
