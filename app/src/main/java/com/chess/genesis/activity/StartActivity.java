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

import android.app.*;
import android.content.*;
import android.os.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class StartActivity extends Activity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// run upgrade
		UpgradeHandler.run(this);

		// set layout mode
		final boolean isTablet = Pref.getBool(this, R.array.pf_tabletMode);

		if (isTablet)
			startActivity(new Intent(this, MainMenuTablet.class));
		else
			startActivity(new Intent(this, MainMenuPhone.class));
	}

	@Override
	public void onResume()
	{
		super.onResume();
		finish();
	}
}