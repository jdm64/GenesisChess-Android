package com.chess.genesis;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class MainMenuTablet extends BaseActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_tablet);

		final BaseContentFrag frag = new MainMenuFrag();
		final MenuBarFrag menubar = new MenuBarFrag(true);

		frag.setMenuBarFrag(menubar);

		getSupportFragmentManager().beginTransaction()
		.replace(R.id.topbar01, menubar)
		.replace(R.id.botbar01, new BotBarFrag())
		.replace(R.id.topbar02, new TopBarFrag())
		.replace(R.id.botbar02, new BotBarFrag())
		.replace(R.id.topbar03, new TopBarFrag())
		.replace(R.id.botbar03, new BotBarFrag())
		.replace(R.id.panel01, frag, MainMenuFrag.TAG)
		.commit();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		// BUG: requestCode is wrong
	//	if (requestCode == Enums.IMPORT_GAME) {
			final FragmentManager fragMan = getSupportFragmentManager();
			final GameListLocalFrag frag = (GameListLocalFrag) fragMan.findFragmentByTag(GameListLocalFrag.TAG);

			frag.recieveGame(data);
	//	}
	}
}
