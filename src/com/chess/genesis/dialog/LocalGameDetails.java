package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

class LocalGameDetails extends BaseDialog implements OnClickListener
{
	private final Bundle gamedata;

	public LocalGameDetails(final Context context, final Bundle data)
	{
		super(context);

		gamedata = data;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Local Game Details");
		setBodyView(R.layout.dialog_gamedetails_local);
		setButtonTxt(R.id.ok, "Save To File");
		setButtonTxt(R.id.cancel, "Close");

		RobotoText txt = (RobotoText) findViewById(R.id.name);
		txt.setText(gamedata.getString("name"));

		txt = (RobotoText) findViewById(R.id.gametype);
		txt.setText(Enums.GameType(Integer.valueOf(gamedata.getString("gametype"))));
		txt = (RobotoText) findViewById(R.id.opponent);
		txt.setText(Enums.OpponentType(Integer.valueOf(gamedata.getString("opponent"))));

		txt = (RobotoText) findViewById(R.id.ctime);
		txt.setText((new PrettyDate(gamedata.getString("ctime"))).agoFormat());
		txt = (RobotoText) findViewById(R.id.stime);
		txt.setText((new PrettyDate(gamedata.getString("stime"))).agoFormat());

		txt = (RobotoText) findViewById(R.id.zfen);
		txt.setText(gamedata.getString("zfen"));
		txt = (RobotoText) findViewById(R.id.history);
		txt.setText(gamedata.getString("history"));
	}

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
		final String filename = "genesischess-" + gamedata.getString("name") + ".txt";
		final String str = GameParser.parse(gamedata).toString();

		FileUtils.writeFile(filename, str);

		Toast.makeText(getContext(), "File Written To:\n" + filename, Toast.LENGTH_LONG).show();
	} catch (JSONException e) {
		Toast.makeText(getContext(), "Game Data Corrupt", Toast.LENGTH_LONG).show();
	} catch (FileNotFoundException e) {
		Toast.makeText(getContext(), "Can Not Open File", Toast.LENGTH_LONG).show();
	} catch (IOException e) {
		Toast.makeText(getContext(), "I/O Error", Toast.LENGTH_LONG).show();
	}
	}
}
