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
import java.util.stream.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class MoveFlagsTest
{
	@Test
	void testEnPassant()
	{
		var flags = new MoveFlags();

		flags.setCastle(MoveFlags.WK_CASTLE | MoveFlags.BQ_CASTLE);

		flags.setEnPassant(4);
		assertTrue(flags.canEnPassant());
		assertEquals(4, flags.enPassantFile());
		assertTrue(flags.canKingCastle(Piece.WHITE));
		assertTrue(flags.canQueenCastle(Piece.BLACK));

		flags.clearEnPassant();
		assertFalse(flags.canEnPassant());
		assertEquals(0, flags.enPassantFile());
		assertTrue(flags.canKingCastle(Piece.WHITE));
		assertTrue(flags.canQueenCastle(Piece.BLACK));
	}

	@ParameterizedTest
	@MethodSource("testCastle_param")
	void testCastle(int bits, boolean whiteKing, boolean blackKing, boolean whiteQueen, boolean blackQueen)
	{
		var flags = new MoveFlags();

		flags.setCastle(bits);

		assertEquals(whiteKing | whiteQueen, flags.canCastle(Piece.WHITE));
		assertEquals(blackKing | blackQueen, flags.canCastle(Piece.BLACK));
		assertEquals(whiteKing, flags.canKingCastle(Piece.WHITE));
		assertEquals(blackKing, flags.canKingCastle(Piece.BLACK));
		assertEquals(whiteQueen, flags.canQueenCastle(Piece.WHITE));
		assertEquals(blackQueen, flags.canQueenCastle(Piece.BLACK));

		flags.clearCastle(Piece.WHITE);

		assertFalse(flags.canCastle(Piece.WHITE));
		assertEquals(blackKing | blackQueen, flags.canCastle(Piece.BLACK));
		assertFalse(flags.canKingCastle(Piece.WHITE));
		assertEquals(blackKing, flags.canKingCastle(Piece.BLACK));
		assertFalse(flags.canQueenCastle(Piece.WHITE));
		assertEquals(blackQueen, flags.canQueenCastle(Piece.BLACK));

		flags.setCastle(bits);
		flags.clearCastle(Piece.BLACK);

		assertEquals(whiteKing | whiteQueen, flags.canCastle(Piece.WHITE));
		assertFalse(flags.canCastle(Piece.BLACK));
		assertEquals(whiteKing, flags.canKingCastle(Piece.WHITE));
		assertFalse(flags.canKingCastle(Piece.BLACK));
		assertEquals(whiteQueen, flags.canQueenCastle(Piece.WHITE));
		assertFalse(flags.canQueenCastle(Piece.BLACK));

		flags.setCastle(bits);
		flags.clearKingCastle(Piece.WHITE);

		assertEquals(whiteQueen, flags.canCastle(Piece.WHITE));
		assertEquals(blackKing | blackQueen, flags.canCastle(Piece.BLACK));
		assertFalse(flags.canKingCastle(Piece.WHITE));
		assertEquals(blackKing, flags.canKingCastle(Piece.BLACK));
		assertEquals(whiteQueen, flags.canQueenCastle(Piece.WHITE));
		assertEquals(blackQueen, flags.canQueenCastle(Piece.BLACK));

		flags.setCastle(bits);
		flags.clearKingCastle(Piece.BLACK);

		assertEquals(whiteKing | whiteQueen, flags.canCastle(Piece.WHITE));
		assertEquals(blackQueen, flags.canCastle(Piece.BLACK));
		assertEquals(whiteKing, flags.canKingCastle(Piece.WHITE));
		assertFalse(flags.canKingCastle(Piece.BLACK));
		assertEquals(whiteQueen, flags.canQueenCastle(Piece.WHITE));
		assertEquals(blackQueen, flags.canQueenCastle(Piece.BLACK));

		flags.setCastle(bits);
		flags.clearQueenCastle(Piece.WHITE);

		assertEquals(whiteKing, flags.canCastle(Piece.WHITE));
		assertEquals(blackKing | blackQueen, flags.canCastle(Piece.BLACK));
		assertEquals(whiteKing, flags.canKingCastle(Piece.WHITE));
		assertEquals(blackKing, flags.canKingCastle(Piece.BLACK));
		assertFalse(flags.canQueenCastle(Piece.WHITE));
		assertEquals(blackQueen, flags.canQueenCastle(Piece.BLACK));

		flags.setCastle(bits);
		flags.clearQueenCastle(Piece.BLACK);

		assertEquals(whiteKing | whiteQueen, flags.canCastle(Piece.WHITE));
		assertEquals(blackKing, flags.canCastle(Piece.BLACK));
		assertEquals(whiteKing, flags.canKingCastle(Piece.WHITE));
		assertEquals(blackKing, flags.canKingCastle(Piece.BLACK));
		assertEquals(whiteQueen, flags.canQueenCastle(Piece.WHITE));
		assertFalse(flags.canQueenCastle(Piece.BLACK));
	}

	static Object[][] testCastle_param()
	{
		return new Object[][] {
		    {0, false, false, false, false},
		    {MoveFlags.BK_CASTLE, false, true, false, false},
		    {MoveFlags.BQ_CASTLE, false, false, false, true},
		    {MoveFlags.B_CASTLE, false, true, false, true},

		    {MoveFlags.WK_CASTLE, true, false, false, false},
		    {MoveFlags.WK_CASTLE | MoveFlags.BK_CASTLE, true, true, false, false},
		    {MoveFlags.WK_CASTLE | MoveFlags.BQ_CASTLE, true, false, false, true},
		    {MoveFlags.WK_CASTLE | MoveFlags.B_CASTLE, true, true, false, true},

		    {MoveFlags.WQ_CASTLE, false, false, true, false},
		    {MoveFlags.WQ_CASTLE | MoveFlags.BK_CASTLE, false, true, true, false},
		    {MoveFlags.WQ_CASTLE | MoveFlags.BQ_CASTLE, false, false, true, true},
		    {MoveFlags.WQ_CASTLE | MoveFlags.B_CASTLE, false, true, true, true},

		    {MoveFlags.W_CASTLE, true, false, true, false},
		    {MoveFlags.W_CASTLE | MoveFlags.BK_CASTLE, true, true, true, false},
		    {MoveFlags.W_CASTLE | MoveFlags.BQ_CASTLE, true, false, true, true},
		    {MoveFlags.WB_CASTLE, true, true, true, true},
		};
	}

	@Test
	void testCopyGet()
	{
		var flags = new MoveFlags();
		flags.setEnPassant(4);
		flags.setCastle(MoveFlags.WK_CASTLE | MoveFlags.BQ_CASTLE);

		var newFlag = new MoveFlags(flags);
		assertTrue(newFlag.canEnPassant());
		assertEquals(4, newFlag.enPassantFile());
		assertTrue(newFlag.canKingCastle(Piece.WHITE));
		assertFalse(newFlag.canKingCastle(Piece.BLACK));
		assertFalse(newFlag.canQueenCastle(Piece.WHITE));
		assertTrue(newFlag.canQueenCastle(Piece.BLACK));

		var freshFlags = newFlag.get();
		assertFalse(freshFlags.canEnPassant());
		assertEquals(4, newFlag.enPassantFile());
		assertTrue(freshFlags.canKingCastle(Piece.WHITE));
		assertTrue(freshFlags.canKingCastle(Piece.BLACK));
		assertTrue(freshFlags.canQueenCastle(Piece.WHITE));
		assertTrue(freshFlags.canQueenCastle(Piece.BLACK));
	}
}
