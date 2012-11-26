/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.dialog;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import java.io.*;
import org.json.*;

public class GameDetailsDialog extends BaseDialog
{
	private final Bundle gamedata;
	private final boolean isOnline;

	public GameDetailsDialog(final Context context, final Bundle data, final boolean _isOnline)
	{
		super(context);
		gamedata = data;
		isOnline = _isOnline;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(isOnline? "Online Game Details" : "Local Game Details");
		setBodyView(isOnline? R.layout.dialog_gamedetails_online : R.layout.dialog_gamedetails_local);
		setButtonTxt(R.id.ok, "Save To File");
		setButtonTxt(R.id.cancel, "Close");

		RobotoText txt;
		if (isOnline) {
			txt = (RobotoText) findViewById(R.id.white);
			txt.setText(gamedata.getString("white"));
			txt = (RobotoText) findViewById(R.id.black);
			txt.setText(gamedata.getString("black"));
			txt = (RobotoText) findViewById(R.id.eventtype);
			txt.setText(Enums.EventType(Integer.parseInt(gamedata.getString("eventtype"))));
			txt = (RobotoText) findViewById(R.id.status);
			txt.setText(Enums.GameStatus(Integer.parseInt(gamedata.getString("status"))));
		} else {
			txt = (RobotoText) findViewById(R.id.name);
			txt.setText(gamedata.getString("name"));
			txt = (RobotoText) findViewById(R.id.opponent);
			txt.setText(Enums.OpponentType(Integer.parseInt(gamedata.getString("opponent"))));
		}
		txt = (RobotoText) findViewById(R.id.gametype);
		txt.setText(Enums.GameType(Integer.parseInt(gamedata.getString("gametype"))));
		txt = (RobotoText) findViewById(R.id.ctime);
		txt.setText(new PrettyDate(gamedata.getString("ctime")).agoFormat());
		txt = (RobotoText) findViewById(R.id.stime);
		txt.setText(new PrettyDate(gamedata.getString("stime")).agoFormat());
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok)
			saveToFile();
		else
			dismiss();
	}

	private void saveToFile()
	{
	try {
		final String gamename = isOnline?
			gamedata.getString("white") + " Vs. " + gamedata.getString("black") :
			gamedata.getString("name");
		final String filename = gamename + ".txt";
		final String str = GameParser.parse(gamedata).toString();

		FileUtils.writeFile(filename, str);

		Toast.makeText(getContext(), "File Written To:\n" + filename, Toast.LENGTH_LONG).show();
	} catch (final JSONException e) {
		Toast.makeText(getContext(), "Game Data Corrupt", Toast.LENGTH_LONG).show();
	} catch (final FileNotFoundException e) {
		Toast.makeText(getContext(), "Can Not Open File", Toast.LENGTH_LONG).show();
	} catch (final IOException e) {
		Toast.makeText(getContext(), "I/O Error", Toast.LENGTH_LONG).show();
	}
	}
}
