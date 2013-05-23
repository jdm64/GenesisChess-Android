/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.activity;

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.view.View.OnClickListener;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public abstract class BaseContentFrag extends Fragment implements OnClickListener
{
	protected BaseActivity act;
	protected FragmentManager fragMan;
	protected MenuBarFrag menuBar;
	protected int containerId;
	protected boolean isTablet = false;

	public abstract String getBTag();

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
	public boolean onContextItemSelected(final MenuItem item)
	{
		return act.lastContextMenu.equals(getBTag())? onOptionsItemSelected(item) : super.onContextItemSelected(item);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		AdsHandler.run(act);
	}

	protected void initBaseContentFrag(final ViewGroup container)
	{
		act = (BaseActivity) getActivity();
		fragMan = getFragmentManager();

		// set the current view id that the fragment lives in
		containerId = container.getId();

		// set what layout we're in
		final View itab = act.findViewById(R.id.panel01);
		isTablet = itab != null;
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
