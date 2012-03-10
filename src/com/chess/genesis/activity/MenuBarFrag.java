package com.chess.genesis;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnLongClickListener;

public class MenuBarFrag extends SimpleFrag implements OnLongClickListener
{
	public final static String TAG = "MENUBAR";

	private FragmentActivity act;
	private FragmentManager fragMan;
	private boolean hasTitle = false;

	public MenuBarFrag()
	{
		super(R.layout.fragment_menubar);
	}

	public MenuBarFrag(final FragmentActivity activity)
	{
		super(R.layout.fragment_menubar_title);

		hasTitle = true;
		act = activity;
		fragMan = act.getSupportFragmentManager();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (hasTitle)
			getView().findViewById(R.id.menu_title).setOnLongClickListener(this);
	}

	public boolean onLongClick(final View v)
	{
		if (fragMan.getBackStackEntryCount() > 0)
			fragMan.popBackStack();
		else
			act.finish();
		return true;
	}
}
