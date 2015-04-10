/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
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

import android.support.v4.app.*;

import java.lang.ref.*;

public class GameListPager extends FragmentPagerAdapter
{
	private final static String[] TITLES = new String[]{"Their Turn", "Your Turn", "Archive Games"};

	private final WeakReference<Fragment>[] list = new WeakReference[] {
		new WeakReference<Fragment>(null),
		new WeakReference<Fragment>(null),
		new WeakReference<Fragment>(null)
	};

	public GameListPager(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public int getCount()
	{
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return TITLES[position];
	}

	@Override
	public Fragment getItem(int position)
	{
		Fragment frag = list[position].get();
		if (frag == null) {
			frag = GameListPage.newInstance(position);
			list[position] = new WeakReference<Fragment>(frag);
		}
		return frag;
	}
}
