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
import android.view.*;
import android.view.View.*;
import com.chess.genesis.*;
import androidx.fragment.app.*;

public abstract class BaseContentFrag extends BaseFrag implements OnClickListener
{
	BaseActivity act;
	FragmentManager fragMan;
	private int containerId;

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
		return getBTag().equals(act.lastContextMenu)? onOptionsItemSelected(item) : super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(final Bundle data)
	{
		super.onCreate(data);
		act = (BaseActivity) getActivity();
		fragMan = getFragmentManager();
	}

	void initBaseContentFrag(final ViewGroup container)
	{
		// set the current view id that the fragment lives in
		containerId = container.getId();
	}

	void openMenu(final View view)
	{
		registerForContextMenu(view);
		act.openContextMenu(view);
		unregisterForContextMenu(view);
	}
}
