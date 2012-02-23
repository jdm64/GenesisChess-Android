package com.chess.genesis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;

abstract class GameListFrag extends BaseContentFrag
{
	abstract public void updateGameList();

	protected void loadGame(final Bundle gamedata)
	{
		if (isTablet) {
			final boolean isOnline = gamedata.containsKey("gameid");
			final int gametype = Integer.valueOf(gamedata.getString("gametype"));
			final MenuBarFrag gameMenu = new MenuBarFrag();
			final GameFrag gameFrag = (gametype == Enums.GENESIS_CHESS)?
				new GenGameFrag() : new RegGameFrag();
			gameFrag.setArguments(gamedata);
			gameFrag.setMenuBarFrag(gameMenu);

			// Pop game if already loaded
			fragMan.popBackStack(GameFrag.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

			FragmentTransaction ftrans = fragMan.beginTransaction()
			.replace(R.id.topbar02, gameMenu, MenuBarFrag.TAG)
			.replace(R.id.botbar02, new BoardNavFrag(), BoardNavFrag.TAG)
			.replace(R.id.panel02, gameFrag, GameFrag.TAG);

			// setup chat window
			if (isOnline) {
				final MenuBarFrag msgMenu = new MenuBarFrag();
				final BaseContentFrag msgFrag = new MsgBoxFrag();
				msgFrag.setArguments(gamedata);
				msgFrag.setMenuBarFrag(msgMenu);

				ftrans = ftrans.replace(R.id.topbar03, msgMenu, MenuBarFrag.TAG)
				.replace(R.id.panel03, msgFrag, MsgBoxFrag.TAG);
			}
			ftrans.addToBackStack(GameFrag.TAG).commit();
		} else {
			final Intent intent = new Intent(act, Game.class);
			intent.putExtras(gamedata);
			startActivity(intent);
		}
	}

	protected void sendGame(final Bundle gamedata)
	{
	try {
		final String gamename =	gamedata.containsKey("gameid")?
			gamedata.getString("white") + " V. " + gamedata.getString("black") :
			gamedata.getString("name");
		final String filename = "genesischess-" + gamename + ".txt";
		final String gamestr = GameParser.export(gamedata).toString();
		final Uri uri = FileUtils.writeFile(filename, gamestr);
		final Intent intent = new Intent(Intent.ACTION_SEND);

		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType("application/json");
		startActivity(intent);
	} catch (JSONException e) {
		Toast.makeText(act, "Corrupt Game Data", Toast.LENGTH_LONG).show();
	} catch (FileNotFoundException e) {
		Toast.makeText(act, "File Not Found", Toast.LENGTH_LONG).show();
	} catch (IOException e) {
		Toast.makeText(act, "Error Reading File", Toast.LENGTH_LONG).show();
	}
	}
}