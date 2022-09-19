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
package com.chess.genesis.net.msgs;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import android.content.*;
import android.util.*;
import org.msgpack.core.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class ActiveGameDataMsg extends ZmqMsg
{
	public static final int ID = 10;

	public String game_id;
	public boolean is_new;
	public int game_type;
	public long create_time;
	public long save_time;
	public String white;
	public String black;
	public String zfen;
	public List<Pair<String,Long>> moves;

	public String movesString()
	{
		return moves.stream().map((p) -> p.first + "," + p.second).collect(Collectors.joining(" "));
	}

	public String getOpponent(Context ctx)
	{
		return Pref.getString(ctx, R.array.pf_username).equals(white) ? black : white;
	}

	public int getOpponentType(Context ctx)
	{
		return Pref.getString(ctx, R.array.pf_username).equals(white) ? Enums.INVITE_BLACK_OPPONENT : Enums.INVITE_WHITE_OPPONENT;
	}

	@Override
	public int type()
	{
		return ID;
	}

	@Override
	ZmqMsg parse(MessageUnpacker packer) throws IOException
	{
		game_id = packer.unpackString();
		is_new = packer.unpackBoolean();
		game_type = packer.unpackInt();
		create_time = packer.unpackLong();
		save_time = packer.unpackLong();
		white = packer.unpackString();
		black = packer.unpackString();
		zfen = packer.unpackString();
		var move_size = packer.unpackArrayHeader();
		moves = new ArrayList<>();
		for (int i = 0; i < move_size; i++) {
			moves.add(new Pair<String,Long>(packer.unpackString(), packer.unpackLong()));
		}
		return this;
	}

	@Override
	void toBytes(MessageBufferPacker packer) throws IOException
	{
		packer.packString(game_id);
		packer.packBoolean(is_new);
		packer.packInt(game_type);
		packer.packLong(create_time);
		packer.packLong(save_time);
		packer.packString(white);
		packer.packString(black);
		packer.packString(zfen);
		packer.packArrayHeader(moves.size());
		for (var pair : moves) {
			packer.packString(pair.first);
			packer.packLong(pair.second);
		}
	}
}
