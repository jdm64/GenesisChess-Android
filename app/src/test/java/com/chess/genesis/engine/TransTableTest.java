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
import java.util.*;
import org.junit.jupiter.api.*;

class TransTableTest
{
	@Test
	void testStartHash()
	{
		var genBoard = new GenBoard();
		new TransTable(genBoard, 2);

		genBoard.reset();
		assertNotEquals(0, genBoard.hash());

		var regBoard = new RegBoard();
		new TransTable(regBoard, 2);

		regBoard.reset();
		assertNotEquals(0, regBoard.hash());
	}

	@Test
	void testSaveLoad()
	{
		var board = new GenBoard();
		var table = new TransTable(board, 2);

		board.reset();
		var move = new Move();
		move.parse("a2a3");

		table.setItem(board.hash(), 5, move, 4, TransItem.PV_NODE);

		TransItem item = new TransItem();
		assertFalse(table.getItem(board.hash() + 1, item));
		assertTrue(table.getItem(board.hash(), item));
		assertEquals("TransItem{move=a2a3, score=5, depth=4, type=7}", item.toString());
	}

	@Test
	void testRand64()
	{
		var rand = new Rand64();
		var set = new HashSet<Long>();
		for (int i = 0; i < 8192; i++) {
			set.add(rand.next());
		}

		assertEquals(8192, set.size());
	}
}
