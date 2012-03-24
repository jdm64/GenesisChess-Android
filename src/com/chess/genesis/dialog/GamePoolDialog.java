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

package com.chess.genesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;

class GamePoolDialog extends BaseDialog implements OnClickListener
{
	private final Context context;
	private final ObjectArray<PoolDataItem> data;

	private static class PoolDataItem
	{
		public String gametype;
		public String time;

		public PoolDataItem(final String GameType, final long Time)
		{
			gametype = GameType.substring(0,1).toUpperCase() + GameType.substring(1);
			time = new PrettyDate(Time).agoFormat();
		}
	}

	public GamePoolDialog(final Context _context)
	{
		super(_context, BaseDialog.CANCEL);
		context = _context;

	try {
		// Load pool info json array
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final JSONArray pool = new JSONArray(pref.getString("poolinfo", "[]"));

		data = new ObjectArray<PoolDataItem>();
		for (int i = 0; i < pool.length(); i++) {
			final String type = pool.getJSONObject(i).getString("gametype");
			final long time = pool.getJSONObject(i).getLong("added");

			data.push(new PoolDataItem(type, time));
		}
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Game Pool Info");
		setBodyView(R.layout.dialog_gamepool);
		setButtonTxt(R.id.cancel, "Close");

		final TableLayout table = (TableLayout) findViewById(R.id.layout01);
		final LayoutParams layout = (TableRow.LayoutParams) findViewById(R.id.left).getLayoutParams();

		for (int i = 0; i < data.size(); i++) {
			final TableRow row = new TableRow(context);

			TextView txt = new TextView(context);
			txt.setLayoutParams(layout);
			txt.setText(data.get(i).gametype);
			row.addView(txt);

			txt = new TextView(context);
			txt.setText(data.get(i).time);
			row.addView(txt);

			table.addView(row);
		}
	}

	public void onClick(final View v)
	{
		dismiss();
	}
}
