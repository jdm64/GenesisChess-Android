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
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junit.platform.commons.util.*;
import com.google.android.gms.common.util.*;
import androidx.core.util.*;

public abstract class AbstractBoardTest
{
	abstract BaseBoard getBoard();

	@ParameterizedTest
	@MethodSource("testParseFen_Params")
	void testParseFen(String zfen, String fen)
	{
		var board = getBoard();

		if (!fen.isEmpty()) {
			board.parseFen(fen);
			assertEquals(zfen, board.printZFen());
		}

		if (!zfen.isEmpty()) {
			board.parseZFen(zfen);
			assertEquals(fen, board.printFen());
		}
	}

	@ParameterizedTest
	@MethodSource("testGenMoves_Params")
	void testGenMoves(String zfen, String cord, String moves, String captures)
	{
		var board = getBoard();

		board.parseZFen(zfen);
		var idx = cordToIdx(cord);

		assertEquals(moves, printCordList(board.genMove(idx)), "genMove() " + board.printFen());
		assertEquals(captures, printCordList(board.genCapture(idx)), "genCapture() " + board.printFen());

		var allMoves = mergeCordList(moves, captures);
		assertEquals(allMoves, printCordList(board.genAll(idx)), "genAll()");
	}

	@ParameterizedTest
	@MethodSource("testParseAndMove_Params")
	void testParseAndMove(String zfen, String moves, String expected)
	{
		var board = getBoard();

		assertTrue(board.parseZFen(zfen), board.printFen());
		var moveList = applyMoves(moves, board);
		assertEquals(expected, board.printZFen());
		Collections.reverse(moveList);
		moveList.forEach(i -> board.unmake(i.first, i.second));
		assertEquals(zfen, board.printZFen());
	}

	@ParameterizedTest
	@MethodSource("testGetters_Params")
	void testGetters(String zfen, String placeCounts, String deadCounts)
	{
		var board = getBoard();

		board.parseZFen(zfen);

		var expPly = Integer.valueOf(zfen.substring(zfen.lastIndexOf(':') + 1));

		assertEquals(expPly, board.getPly());
		assertEquals(expPly % 2 == 0 ? Piece.WHITE : Piece.BLACK, board.getStm());

		var piece = board.getBoardArray()[board.kingIndex(Piece.WHITE)];
		assertEquals(Piece.WHITE_KING, piece);
		piece = board.getBoardArray()[board.kingIndex(Piece.BLACK)];
		assertEquals(Piece.BLACK_KING, piece);

		for (int i = 0; i < 32; i++) {
			var loc = board.pieceLoc(i);
			var typ = board.pieceType(i);

			if (loc < 0) {
				continue;
			}

			assertEquals(typ, board.getBoardArray()[loc]);
		}

		var count = IntArray.toString(board.getPieceCounts(Piece.PLACEABLE));
		assertEquals(placeCounts, count);
		count = IntArray.toString(board.getPieceCounts(Piece.DEAD));
		assertEquals(deadCounts, count);

		assertEquals(zfen, board.copy().printZFen());
	}

	@ParameterizedTest
	@MethodSource("testIsMate_Params")
	void testIsMate(String zfen, int mateType)
	{
		var board = getBoard();

		board.parseZFen(zfen);
		assertEquals(mateType, board.isMate());
	}

	static List<Pair<Move,MoveFlags>> applyMoves(String moves, Board board)
	{
		var moveList = new ArrayList<Pair<Move,MoveFlags>>();
		Arrays.stream(moves.split(" ")).sequential().forEach(m -> {
			var res = board.parseMove(m);
			assertEquals(Board.VALID_MOVE, res.second, m);

			var move = res.first;

			var flags = new MoveFlags();
			board.getMoveFlags(flags);

			moveList.add(new Pair<>(move, flags));
			board.make(move);
		});
		return moveList;
	}

	static int cordToIdx(String cord)
	{
		return Move.cordToIdx(cord.charAt(0), cord.charAt(1));
	}

	static String printCordList(int[] cords)
	{
		var buff = new ArrayList<String>();
		var isEnd = new AtomicBoolean(false);
		Arrays.stream(cords).forEach(i -> {
			if (i < 0) {
				isEnd.set(true);
			} else if (!isEnd.get()) {
				buff.add(Move.printSq(i));
			}
		});
		buff.sort(null);
		return String.join(" ", buff);
	}

	static String mergeCordList(String one, String two)
	{
		var list = new ArrayList<String>(Arrays.asList((one + " " + two).strip().split(" ")));
		list.sort(null);
		return String.join(" ", list);
	}
}
