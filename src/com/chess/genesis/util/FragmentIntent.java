package com.chess.genesis;

import android.support.v4.app.FragmentManager;

class FragmentIntent
{
	private BaseContentFrag frag;
	private String tag;
	private int layoutId;

	public void setFrag(final int LayoutId, final BaseContentFrag fragment, final String Tag)
	{
		layoutId = LayoutId;
		frag = fragment;
		tag = Tag;
	}

	public void loadFrag(final FragmentManager fragMan)
	{
		final MenuBarFrag menuBar = new MenuBarFrag();

		frag.setMenuBarFrag(menuBar);

		int menuLayout;
		switch (layoutId) {
		case R.id.panel01:
			menuLayout = R.id.topbar01;
			menuBar.enableTitle(true);
			break;
		case R.id.panel02:
		default:
			menuLayout = R.id.topbar02;
			break;
		case R.id.panel03:
			menuLayout = R.id.topbar03;
			break;
		}

		fragMan.beginTransaction()
		.replace(menuLayout, menuBar, MenuBarFrag.TAG)
		.replace(layoutId, frag, tag)
		.addToBackStack(tag).commit();
	}
}
