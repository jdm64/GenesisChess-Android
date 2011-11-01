package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class GamePoolDialog extends Dialog implements OnClickListener
{
	private final Context context;
	private final ObjectArray<PoolDataItem> data;

	private class PoolDataItem
	{
		public String gametype;
		public String time;

		public PoolDataItem(String GameType, long Time)
		{
			gametype = GameType.substring(0,1).toUpperCase() + GameType.substring(1);
			time = (new PrettyDate(Time)).agoFormat();
		}
	}

	public GamePoolDialog(final Context _context)
	{
		super(_context);
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
		setTitle("Game Pool Info");

		setContentView(R.layout.gamepool_info);

		final TableLayout table = (TableLayout) findViewById(R.id.table01);
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

		Button button = (Button) findViewById(R.id.close);
		button.setOnClickListener(this);
	}

	public void onClick(final View v)
	{
		dismiss();
	}
}