/* source: http://code.google.com/p/android-playground/source/browse/trunk/SwipeyTabsSample/src/net/peterkuterna/android/apps/swipeytabs/SwipeyTabFragment.java
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class SwipeTabFragment extends Fragment
{
	public SwipeTabFragment(final String title)
	{
		super();

		final Bundle args = new Bundle();
		args.putString("title", title);
		setArguments(args);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.swipetab_fragment, null);
		final String title = getArguments().getString("title");
		((TextView) root.findViewById(R.id.text)).setText(title);
		return root;
	}
}