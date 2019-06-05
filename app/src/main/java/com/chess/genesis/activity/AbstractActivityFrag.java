/* GenesisChess, an Android chess application
 * Copyright 2018, Justin Madru (justin.jdm64@gmail.com)
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
import android.os.Handler.*;
import android.support.v4.app.*;

public abstract class AbstractActivityFrag extends BaseFrag implements Callback
{
	FragmentActivity act;
	FragmentManager fragMan;

	@Override
	public void onCreate(final Bundle data)
	{
		super.onCreate(data);

		act = getActivity();
		fragMan = getFragmentManager();
		setHasOptionsMenu(true);
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		return false;
	}
}
