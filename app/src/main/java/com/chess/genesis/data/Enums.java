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
import java.util.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.processor.*;

@EnumsConst
public interface Enums<T extends Enum<T>>
{
	int getId();
	String getName();

	static <E extends Enum<E> & Enums<E>> E from(Class<E> enumClass, int id)
	{
		for (E e : Objects.requireNonNull(enumClass.getEnumConstants())) {
			if (e.getId() == id) return e;
		}
		throw new IllegalArgumentException("Unknown id: " + id + " for: " + enumClass.getSimpleName());
	}

	static <E extends Enum<E> & Enums<E>> E from(Class<E> enumClass, String name)
	{
		for (E e : Objects.requireNonNull(enumClass.getEnumConstants())) {
			if (e.getName().equals(name)) return e;
		}
		throw new IllegalArgumentException("Unknown name: " + name + " for: " + enumClass.getSimpleName());
	}

	enum GameType implements Enums<GameType>
	{
		ANY(0, "any"),
		REGULAR(1, "regular"),
		GENESIS(2, "genesis");

		public final int id;
		public final String name;

		GameType(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public GameType norm()
		{
			if (this == ANY) {
				return new SecureRandom().nextBoolean() ? REGULAR : GENESIS;
			} else {
				return this;
			}
		}

		@Override public int getId() { return id; }
		@Override public String getName() { return name; }
	}

	enum OpponentCat implements Enums<OpponentCat>
	{
		HUMAN(1, "human"),
		CPU(2, "cpu"),
		REMOTE(3, "remote");

		public final int id;
		public final String name;

		OpponentCat(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override public int getId() { return id; }
		@Override public String getName() { return name; }
	}

	enum OpponentType implements Enums<OpponentType>
	{
		HUMAN(1, "human", Piece.EMPTY),
		CPU_WHITE(2, "cpu-white", Piece.BLACK),
		CPU_BLACK(3, "cpu-black", Piece.WHITE),
		REMOTE_WHITE(4, "remote-white", Piece.BLACK),
		REMOTE_BLACK(5, "remote-black", Piece.WHITE);

		public final int id;
		public final String name;
		public final int yourColor;

		OpponentType(int id, String name, int yourColor) {
			this.id = id;
			this.name = name;
			this.yourColor = yourColor;
		}

		public static OpponentType from(OpponentCat oppCat, ColorType color)
		{
			if (color == ColorType.RANDOM) {
				color = new SecureRandom().nextBoolean() ? ColorType.WHITE : ColorType.BLACK;
			}

			return switch (oppCat) {
				case CPU ->
					color == ColorType.WHITE ? OpponentType.CPU_BLACK : OpponentType.CPU_WHITE;
				case REMOTE ->
					color == ColorType.WHITE ? OpponentType.REMOTE_BLACK : OpponentType.REMOTE_WHITE;
				default -> OpponentType.HUMAN;
			};
		}

		@Override public int getId() { return id; }
		@Override public String getName() { return name; }
	}

	enum EventType implements Enums<EventType>
	{
		LOCAL(0, "local"),
		INVITE(1, "invite"),
		MATCHED(2, "matched");

		public final int id;
		public final String name;

		EventType(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override public int getId() { return id; }
		@Override public String getName() { return name; }
	}

	enum ColorType implements Enums<ColorType>
	{
		RANDOM(0, "random"),
		WHITE(1, "white"),
		BLACK(2, "black");

		public final int id;
		public final String name;

		ColorType(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public ColorType norm() {
			if (this == RANDOM) {
				return new SecureRandom().nextBoolean() ? WHITE : BLACK;
			} else {
				return this;
			}
		}

		@Override public int getId() { return id; }

		@Override public String getName() { return name; }
	}

	enum GameStatus implements Enums<GameStatus>
	{
		WAITING_FOR_OPPONENT(-1, "waiting-for-opponent"),
		ACTIVE(1, "active"),
		WHITE_MATE(2, "white-mate"),
		BLACK_MATE(3, "black-mate"),
		STALEMATE(4, "stalemate");

		public final int id;
		public final String name;

		GameStatus(int id, String name)
		{
			this.id = id;
			this.name = name;
		}

		@Override public int getId() {
			return id;
		}
		@Override public String getName() {
			return name;
		}
	}

	enum ClockType implements Enums<ClockType>
	{
		NO_CLOCK(1, "no-clock"),
		REALTIME(2, "realtime"),
		PER_MOVE(3, "per-move");

		public final int id;
		public final String name;

		ClockType(int id, String name)
		{
			this.id = id;
			this.name = name;
		}

		@Override public int getId() {
			return id;
		}

		@Override public String getName() {
			return name;
		}
	}

	enum ClockTimes implements Enums<ClockTimes>
	{
		SEC_0(0, "0 sec"),
		SEC_1(1, "1 sec"),
		SEC_2(2, "2 sec"),
		SEC_5(5, "5 sec"),
		SEC_10(10, "10 sec"),
		SEC_20(20, "20 sec"),

		MIN_2(120, "2 min"),
		MIN_3(180, "3 min"),
		MIN_5(300, "5 min"),
		MIN_10(600, "10 min"),
		MIN_15(900, "15 min"),
		MIN_30(1800, "30 min"),
		MIN_60(3600, "60 min"),
		MIN_90(5400, "90 min"),

		HRS_12(43200, "12 hrs"),
		DAY_1(86400, "1 day"),
		DAY_3(259200, "3 days");

		public final int time;
		public final String name;

		ClockTimes(int time, String name)
		{
			this.time = time;
			this.name = name;
		}

		public static String from(int time, ClockTimes def)
		{
			for (ClockTimes t : values()) {
				if (t.time == time) return t.name;
			}
			return def.name;
		}

		@Override public int getId() { return time; }
		@Override public String getName() { return name; }
	}
}
