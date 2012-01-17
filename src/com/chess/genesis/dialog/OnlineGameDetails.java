package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;

class OnlineGameDetails extends BaseDialog implements OnClickListener
{
	private final Bundle gamedata;

	public OnlineGameDetails(final Context context, final Bundle data)
	{
		super(context);

		gamedata = data;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Online Game Details");
		setBodyView(R.layout.dialog_gamedetails_online);
		setButtonTxt(R.id.ok, "Save To File");
		setButtonTxt(R.id.cancel, "Close");

		RobotoText txt = (RobotoText) findViewById(R.id.white);
		txt.setText(gamedata.getString("white"));
		txt = (RobotoText) findViewById(R.id.black);
		txt.setText(gamedata.getString("black"));

		txt = (RobotoText) findViewById(R.id.gametype);
		txt.setText(Enums.GameType(Integer.valueOf(gamedata.getString("gametype"))));
		txt = (RobotoText) findViewById(R.id.eventtype);
		txt.setText(Enums.EventType(Integer.valueOf(gamedata.getString("eventtype"))));
		txt = (RobotoText) findViewById(R.id.status);
		txt.setText(Enums.GameStatus(Integer.valueOf(gamedata.getString("status"))));

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
		final String gamename = gamedata.getString("white") + " Vs. " + gamedata.getString("black");
		final String filename = "genesischess-" + gamename + ".txt";
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
