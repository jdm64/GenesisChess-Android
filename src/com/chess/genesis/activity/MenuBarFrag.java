/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.activity;

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.view.View.OnLongClickListener;
import com.chess.genesis.*;

public class MenuBarFrag extends SimpleFrag implements OnLongClickListener
{
	private final static String TAG = "MENUBAR";

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
	public String getBTag()
	{
		return TAG;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (hasTitle)
			getView().findViewById(R.id.menu_title).setOnLongClickListener(this);
	}

	@Override
	public boolean onLongClick(final View v)
	{
		if (fragMan.getBackStackEntryCount() > 0)
			fragMan.popBackStack();
		else
			act.finish();
		return true;
	}
}
