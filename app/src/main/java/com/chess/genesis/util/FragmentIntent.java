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

package com.chess.genesis.util;

import android.support.v4.app.*;
import com.chess.genesis.*;
import com.chess.genesis.activity.*;

public class FragmentIntent
{
	private BaseContentFrag frag;
	private String tag;
	private int layoutId;

	public FragmentIntent()
	{
	}

	public void setFrag(final int LayoutId, final BaseContentFrag fragment)
	{
		layoutId = LayoutId;
		frag = fragment;
		tag = fragment.getBTag();
	}

	public void loadFrag(final FragmentManager fragMan)
	{
		final MenuBarFrag menuBar;
		final int menuLayout;

		switch (layoutId) {
		case R.id.panel01:
			menuLayout = R.id.topbar01;
			menuBar = MenuBarFrag.newInstance(true);
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
		.replace(menuLayout, menuBar, menuBar.getBTag())
		.replace(layoutId, frag, tag)
		.addToBackStack(tag).commit();
	}
}
