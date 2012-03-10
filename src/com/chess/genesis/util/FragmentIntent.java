package com.chess.genesis;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

class FragmentIntent
{
	private FragmentActivity act;
	private BaseContentFrag frag;
	private String tag;
	private int layoutId;

	public void setActivity(final FragmentActivity activity)
	{
		act = activity;
	}

	public void setFrag(final int LayoutId, final BaseContentFrag fragment, final String Tag)
	{
		layoutId = LayoutId;
		frag = fragment;
		tag = Tag;
	}

	public void loadFrag(final FragmentManager fragMan)
	{
		final MenuBarFrag menuBar;
		final int menuLayout;

		switch (layoutId) {
		case R.id.panel01:
			menuLayout = R.id.topbar01;
			menuBar = new MenuBarFrag(act);
			break;
		case R.id.panel02:
		default:
			menuBar = new MenuBarFrag();
			menuLayout = R.id.topbar02;
			break;
		case R.id.panel03:
			menuBar = new MenuBarFrag();
			menuLayout = R.id.topbar03;
			break;
		}
		frag.setMenuBarFrag(menuBar);

		fragMan.beginTransaction()
		.replace(menuLayout, menuBar, MenuBarFrag.TAG)
		.replace(layoutId, frag, tag)
		.addToBackStack(tag).commit();
	}
}
