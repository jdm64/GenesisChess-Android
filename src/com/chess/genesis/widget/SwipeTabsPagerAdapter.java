/* source: http://code.google.com/p/android-playground/source/browse/trunk/SwipeyTabsSample/src/net/peterkuterna/android/apps/swipeytabs/SwipeyTabsSampleActivity.java
 * version: r3
 * 
 * Copyright 2011 Peter Kuterna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

class SwipeTabsPagerAdapter extends FragmentPagerAdapter implements SwipeTabs.Adapter
{
	private final Context mContext;
	private String[] TITLES;
	private ViewPager mViewPager;

	public SwipeTabsPagerAdapter(final Context context, final FragmentManager fm)
	{
		super(fm);
		this.mContext = context;
	}

	public void setTitles(final String[] titles)
	{
		TITLES = titles;
	}

	public void setViewPager(final ViewPager viewPager)
	{
		mViewPager = viewPager;
	}

	@Override
	public Fragment getItem(final int position)
	{
		return new SwipeTabFragment(TITLES[position]);
	}

	@Override
	public int getCount()
	{
		return TITLES.length;
	}

	public TextView getTab(final int position, final SwipeTabs root)
	{
		final TextView view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.swipetab_indicator, root, false);
		view.setText(TITLES[position]);
		view.setOnClickListener(new OnClickListener()
		{
			public void onClick(final View v)
			{
				mViewPager.setCurrentItem(position);
			}
		});
		return view;
	}
}
