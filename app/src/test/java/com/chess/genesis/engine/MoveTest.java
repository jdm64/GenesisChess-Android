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
package com.chess.genesis.engine;

import static org.junit.jupiter.api.Assertions.*;
import android.os.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class MoveTest
{
	@ParameterizedTest
	@MethodSource("testParse_params")
	void testParse(String moveStr, boolean valid, int from, int to, int castle, int place, int promote)
	{
		var move = new Move();
		var isOk = move.parse(moveStr);

		assertEquals(valid, isOk, "Valid error");
		assertEquals(from, move.from, "From error");
		assertEquals(to, move.to, "To error");
		assertEquals(castle, move.getCastle(), "Castle error");
		assertEquals(place, move.getPlace(), "Place error");
		assertEquals(promote, move.getPromote(), "Promote error");

		if (isOk) {
			assertEquals(moveStr, move.toString());

			var parcel = MockParcel.create();
			move.writeToParcel(parcel, 0);
			var newMove = new Move(parcel);
			assertEquals(moveStr, newMove.toString());
		}
	}

	public static Object[][] testParse_params()
	{
		return new Object[][] {
		    // corner squares
		    {"a1a8", true, Piece.A1, Piece.A8, 0, 0, 0},
		    {"a8h8", true, Piece.A8, Piece.H8, 0, 0, 0},
		    {"h8h1", true, Piece.H8, Piece.H1, 0, 0, 0},
		    {"h1a1", true, Piece.H1, Piece.A1, 0, 0, 0},

		    // invalid chars
		    {"`1a8", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},
		    {"i1a8", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},
		    {"a0b2", false, Piece.NULL_MOVE, Piece.B2, 0, 0, 0},
		    {"a9c3", false, Piece.NULL_MOVE, Piece.C3, 0, 0, 0},
		    {"d4`8", false, Piece.D4, Piece.NULL_MOVE, 0, 0, 0},
		    {"e5i8", false, Piece.E5, Piece.NULL_MOVE, 0, 0, 0},
		    {"f6a0", false, Piece.F6, Piece.NULL_MOVE, 0, 0, 0},
		    {"g7a9", false, Piece.G7, Piece.NULL_MOVE, 0, 0, 0},
		    {"tvWm", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},

		    // castle
		    {"O-O", true, Piece.NULL_MOVE, Piece.NULL_MOVE, Move.CASTLE_KS, 0, 0},
		    {"O-O-O", true, Piece.NULL_MOVE, Piece.NULL_MOVE, Move.CASTLE_QS, 0, 0},
		    {"O+O", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},
		    {"O-r", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},
		    {"O-O:O", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},
		    {"O-O-q", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, 0, 0},

		    // placement
		    {"Kb7", true, Piece.PLACEABLE, Piece.B7, 0, Piece.KING, 0},
		    {"Qc6", true, Piece.PLACEABLE, Piece.C6, 0, Piece.QUEEN, 0},
		    {"Rd5", true, Piece.PLACEABLE, Piece.D5, 0, Piece.ROOK, 0},
		    {"Be4", true, Piece.PLACEABLE, Piece.E4, 0, Piece.BISHOP, 0},
		    {"Nf3", true, Piece.PLACEABLE, Piece.F3, 0, Piece.KNIGHT, 0},
		    {"Pg2", true, Piece.PLACEABLE, Piece.G2, 0, Piece.PAWN, 0},
		    {"Ah3", false, Piece.NULL_MOVE, Piece.NULL_MOVE, 0, Piece.EMPTY, 0},

		    // promote
		    {"a7a8K", false, Piece.A7, Piece.A8, 0, 0, Piece.EMPTY},
		    {"a7a8P", false, Piece.A7, Piece.A8, 0, 0, Piece.EMPTY},
		    {"a7a8N", true, Piece.A7, Piece.A8, 0, 0, Piece.KNIGHT},
		    {"a7a8B", true, Piece.A7, Piece.A8, 0, 0, Piece.BISHOP},
		    {"a7a8R", true, Piece.A7, Piece.A8, 0, 0, Piece.ROOK},
		    {"a7a8Q", true, Piece.A7, Piece.A8, 0, 0, Piece.QUEEN},
		    {"a7a8T", false, Piece.A7, Piece.A8, 0, 0, Piece.EMPTY},
		};
	}

	@Test
	void testSetters()
	{
		var move = new Move();

		assertFalse(move.parse(null));
		assertFalse(move.parse(""));

		move.setEnPassant();
		assertTrue(move.getEnPassant());

		move.setCastle(Move.CASTLE_QS);
		assertEquals(Move.CASTLE_QS, move.getCastle());
		move.setCastle(Move.CASTLE_KS);
		assertEquals(Move.CASTLE_KS, move.getCastle());

		move.setPlace(Piece.ROOK);
		assertEquals(Piece.ROOK, move.getPlace());

		move.setPromote(Piece.BISHOP);
		assertEquals(Piece.BISHOP, move.getPromote());

		assertTrue(move.isNull());

		move.index = 1;
		assertFalse(move.isNull());
		move.setNull();
		assertTrue(move.isNull());
	}

	@Test
	void testParcelCreate()
	{
		var move = new Move();
		move.parse("a1b2");
		var parcel = MockParcel.create();
		move.writeToParcel(parcel, 0);

		var moveFromParcel = Move.CREATOR.createFromParcel(parcel);
		assertEquals("a1b2", moveFromParcel.toString());
		assertEquals(0, moveFromParcel.describeContents());

		var moveArr = Move.CREATOR.newArray(37);
		assertEquals(37, moveArr.length);
	}
}
