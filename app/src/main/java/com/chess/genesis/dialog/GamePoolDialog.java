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

import java.util.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.util.*;

public class GamePoolDialog extends BaseDialog
{
	private final Context context;
	private final List<PoolDataItem> data;

	private static class PoolDataItem
	{
		public final String gametype;
		public final String time;

		public PoolDataItem(final String GameType, final long Time)
		{
			gametype = GameType.substring(0,1).toUpperCase(Locale.US) + GameType.substring(1);
			time = new PrettyDate(Time).agoFormat();
		}
	}

	public GamePoolDialog(final Context _context)
	{
		super(_context, BaseDialog.CANCEL);
		context = _context;

	try {
		// Load pool info json array
		final JSONArray pool = new JSONArray(Pref.getString(context, R.array.pf_poolinfo));

		data = new ArrayList<>(pool.length());
		for (int i = 0, len = pool.length(); i < len; i++) {
			final String type = pool.getJSONObject(i).getString("gametype");
			final long time = pool.getJSONObject(i).getLong("added");

			data.add(new PoolDataItem(type, time));
		}
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Game Pool Info");
		setBodyView(R.layout.dialog_gamepool);
		setButtonTxt(R.id.cancel, "Close");

		final TableLayout table = findViewById(R.id.layout01);
		View left = findViewById(R.id.left);
		final ViewGroup.LayoutParams layout = left.getLayoutParams();

		for (final PoolDataItem item : data) {
			final TableRow row = new TableRow(context);

			TextView txt = new TextView(context);
			txt.setLayoutParams(layout);
			txt.setText(item.gametype);
			row.addView(txt);

			txt = new TextView(context);
			txt.setText(item.time);
			row.addView(txt);

			table.addView(row);
		}
	}

	@Override
	public void onClick(final View v)
	{
		dismiss();
	}
}
