package com.chess.genesis;

public class MenuBarFrag extends SimpleFrag
{
	public final static String TAG = "MENUBAR";

	public MenuBarFrag()
	{
		super(R.layout.fragment_menubar);
	}

	public MenuBarFrag(final boolean enableTitle)
	{
		super(enableTitle? R.layout.fragment_menubar_title : R.layout.fragment_menubar);
	}

	public void enableTitle(final boolean value)
	{
		layout = value? R.layout.fragment_menubar_title : R.layout.fragment_menubar;
	}
}
