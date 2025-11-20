/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.data;

import java.security.*;
import com.chess.genesis.engine.*;

public final class Enums
{
	private Enums()
	{
	}

	// Game Types
	public final static int ANY_CHESS = 0;
	public final static int REGULAR_CHESS = 1;
	public final static int GENESIS_CHESS = 2;

	// Opponent Types
	public final static int HUMAN_OPPONENT = 1;
	public final static int CPU_WHITE_OPPONENT = 2;
	public final static int CPU_BLACK_OPPONENT = 3;
	public final static int INVITE_WHITE_OPPONENT = 4;
	public final static int INVITE_BLACK_OPPONENT = 5;
	public final static int CPU_OPPONENT = -1;
	public final static int INVITE_OPPONENT = -2;

	// Opponent Selection
	public final static int RANDOM = 1;
	public final static int INVITE = 2;

	// Color Types
	public final static int WHITE_OPP = 1;
	public final static int BLACK_OPP = 2;
	public final static int RANDOM_OPP = 3;

	// Play As Color
	public final static int PLAY_AS_WHITE = 1;
	public final static int PLAY_AS_BLACK = 2;

	// Game Status
	public final static int ACTIVE = 1;
	public final static int WHITE_MATE = 2;
	public final static int BLACK_MATE = 3;
	public final static int STALEMATE = 4;
	public final static int IMPOSSIBLE = 5;
	public final static int WHITE_RESIGN = 6;
	public final static int BLACK_RESIGN = 7;
	public final static int WHITE_IDLE = 8;
	public final static int BLACK_IDLE = 9;
	public final static int DRAW = 10;

	public static String OpponentType(final int opponent)
	{
		return switch (opponent) {
			case CPU_WHITE_OPPONENT -> "cpu-white";
			case CPU_BLACK_OPPONENT -> "cpu-black";
			case INVITE_WHITE_OPPONENT -> "invite-white";
			case INVITE_BLACK_OPPONENT -> "invite-black";
			default -> "human";
		};
	}

	public static int OpponentType(final String opponent)
	{
		return switch (opponent) {
			case "cpu-white" -> CPU_WHITE_OPPONENT;
			case "cpu-black" -> CPU_BLACK_OPPONENT;
			case "invite-white" -> INVITE_WHITE_OPPONENT;
			case "invite-black" -> INVITE_BLACK_OPPONENT;
			default -> HUMAN_OPPONENT;
		};
	}

	public static int OppToPlayAs(int value)
	{
		return switch (value) {
			case WHITE_OPP -> PLAY_AS_BLACK;
			case BLACK_OPP -> PLAY_AS_WHITE;
			default -> new SecureRandom().nextBoolean() ? PLAY_AS_WHITE : PLAY_AS_BLACK;
		};
	}

	public static int OppToYourColor(int value)
	{
		return switch (value) {
			case CPU_BLACK_OPPONENT, INVITE_BLACK_OPPONENT -> Piece.WHITE;
			case CPU_WHITE_OPPONENT, INVITE_WHITE_OPPONENT -> Piece.BLACK;
			default -> Piece.EMPTY;
		};
	}

	public static String ColorType(final int color)
	{
		return switch (color) {
			case WHITE_OPP -> "white";
			case BLACK_OPP -> "black";
			default -> "random";
		};
	}

	public static int EventType(final String eventtype)
	{
		return switch (eventtype) {
			case "random" -> RANDOM;
			case "invite" -> INVITE;
			default -> throw new RuntimeException("unknown eventtype: " + eventtype);
		};
	}

	public static String EventType(final int eventtype)
	{
		return switch (eventtype) {
			case RANDOM -> "random";
			case INVITE -> "invite";
			default -> throw new RuntimeException("unknown eventtype: " + eventtype);
		};
	}

	public static int GameType(final String gametype)
	{
		return switch (gametype) {
			case "genesis" -> GENESIS_CHESS;
			case "regular" -> REGULAR_CHESS;
			default -> throw new RuntimeException("unknown gametype: " + gametype);
		};
	}

	public static String GameType(final int gametype)
	{
		return switch (gametype) {
			case GENESIS_CHESS -> "genesis";
			case REGULAR_CHESS -> "regular";
			case ANY_CHESS -> "any";
			default -> throw new RuntimeException("unknown gametype: " + gametype);
		};
	}

	public static int GameStatus(final String gamestatus)
	{
		return switch (gamestatus) {
			case "active" -> ACTIVE;
			case "white-mate" -> WHITE_MATE;
			case "black-mate" -> BLACK_MATE;
			case "stalemate" -> STALEMATE;
			case "impossible" -> IMPOSSIBLE;
			case "white-resign" -> WHITE_RESIGN;
			case "black-resign" -> BLACK_RESIGN;
			case "white-idle" -> WHITE_IDLE;
			case "black-idle" -> BLACK_IDLE;
			case "draw" -> DRAW;
			default -> throw new RuntimeException("unknown gamestatus: " + gamestatus);
		};
	}

	public static String GameStatus(final int gamestatus)
	{
		return switch (gamestatus) {
			case ACTIVE -> "active";
			case WHITE_MATE -> "white-mate";
			case BLACK_MATE -> "black-mate";
			case STALEMATE -> "stalemate";
			case IMPOSSIBLE -> "impossible";
			case WHITE_RESIGN -> "white-resign";
			case BLACK_RESIGN -> "black-resign";
			case WHITE_IDLE -> "white-idle";
			case BLACK_IDLE -> "black-idle";
			case DRAW -> "draw";
			default -> throw new RuntimeException("unknown gamestatus: " + gamestatus);
		};
	}
}
