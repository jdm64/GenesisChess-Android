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

package com.chess.genesis.dialog;

import java.io.*;
import java.util.Map.*;
import android.app.AlertDialog.*;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.*;
import com.chess.genesis.util.FileUtils;
import androidx.fragment.app.DialogFragment;

public class GameDetailsDialog extends DialogFragment implements OnClickListener
{
	private Bundle gamedata;
	private boolean isOnline;

	public static GameDetailsDialog create(Bundle data, boolean isOnline)
	{
		GameDetailsDialog dialog = new GameDetailsDialog();
		dialog.gamedata = data;
		dialog.isOnline = isOnline;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this,
			isOnline? R.layout.dialog_gamedetails_online : R.layout.dialog_gamedetails_local);

		builder.getValue()
		    .setTitle(isOnline? "Online Game Details" : "Local Game Details")
		    .setPositiveButton("Save To File", this)
		    .setNegativeButton("Close", this);

		View view = builder.getKey();

		TextView txt;
		if (isOnline) {
			txt = view.findViewById(R.id.white);
			txt.setText(gamedata.getString("white"));
			txt = view.findViewById(R.id.black);
			txt.setText(gamedata.getString("black"));
			txt = view.findViewById(R.id.eventtype);
			txt.setText(Enums.EventType(Integer.parseInt(gamedata.getString("eventtype"))));
			txt = view.findViewById(R.id.status);
			txt.setText(Enums.GameStatus(Integer.parseInt(gamedata.getString("status"))));
		} else {
			txt = view.findViewById(R.id.name);
			txt.setText(gamedata.getString("name"));
			txt = view.findViewById(R.id.opponent);
			txt.setText(Enums.OpponentType(Integer.parseInt(gamedata.getString("opponent"))));
		}
		txt = view.findViewById(R.id.gametype);
		txt.setText(Enums.GameType(Integer.parseInt(gamedata.getString("gametype"))));
		txt = view.findViewById(R.id.ctime);
		txt.setText(new PrettyDate(gamedata.getString("ctime")).agoFormat());
		txt = view.findViewById(R.id.stime);
		txt.setText(new PrettyDate(gamedata.getString("stime")).agoFormat());
		txt = view.findViewById(R.id.zfen);
		txt.setText(gamedata.getString("zfen"));
		txt = view.findViewById(R.id.history);
		txt.setText(gamedata.getString("history"));

		return builder.getValue().create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			saveToFile();
		} else {
			dismiss();
		}
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
