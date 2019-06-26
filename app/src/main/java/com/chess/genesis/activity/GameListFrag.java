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

package com.chess.genesis.activity;

import java.io.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.FileUtils;

abstract class GameListFrag extends AbstractActivityFrag
{
	abstract public void updateGameList();

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Bundle data = (Bundle) parent.getItemAtPosition(position);

		loadGame(data);
	}

	void loadGame(final Bundle gamedata)
	{
		final Intent intent = new Intent(act, Game.class);
		intent.putExtras(gamedata);
		startActivity(intent);
	}

	void sendGame(final Bundle gamedata)
	{
	try {
		final String gamename =	gamedata.containsKey("gameid")?
			gamedata.getString("white") + " V. " + gamedata.getString("black") :
			gamedata.getString("name");
		final String filename = gamename + ".txt";
		final String gamestr = GameParser.export(gamedata).toString();
		final Uri uri = FileUtils.writeFile(filename, gamestr);
		final Intent intent = new Intent(Intent.ACTION_SEND);

		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType("application/json");
		startActivity(intent);
	} catch (final JSONException e) {
		Toast.makeText(act, "Corrupt Game Data", Toast.LENGTH_LONG).show();
	} catch (final FileNotFoundException e) {
		Toast.makeText(act, "File Not Found", Toast.LENGTH_LONG).show();
	} catch (final IOException e) {
		Toast.makeText(act, "Error Reading File", Toast.LENGTH_LONG).show();
	}
	}
}
