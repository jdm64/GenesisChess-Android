package com.chess.genesis;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;

abstract class BaseContentFrag extends Fragment implements OnClickListener
{
	protected BaseActivity act;
	protected FragmentManager fragMan;
	protected MenuBarFrag menuBar;
	protected boolean isTablet = false;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (menuBar != null) {
			final View view = menuBar.getView().findViewById(R.id.menu);
			view.setOnClickListener(this);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		AdsHandler.run(act);
	}

	protected void initBaseContentFrag()
	{
		act = (BaseActivity) getActivity();
		fragMan = getFragmentManager();

		// set what layout we're in
		final View itab = act.findViewById(R.id.panel01);
		isTablet = (itab != null)? true : false;
	}

	public void setMenuBarFrag(final MenuBarFrag MenuBar)
	{
		menuBar = MenuBar;
	}

	public void openMenu(final View view)
	{
		registerForContextMenu(view);
		act.openContextMenu(view);
		unregisterForContextMenu(view);
	}
}
