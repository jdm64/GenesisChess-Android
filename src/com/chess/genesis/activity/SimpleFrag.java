package com.chess.genesis;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

abstract class SimpleFrag extends Fragment
{
	protected int layout;

	public SimpleFrag(final int layoutId)
	{
		super();

		layout = layoutId;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(layout, container, false);
	}
}
