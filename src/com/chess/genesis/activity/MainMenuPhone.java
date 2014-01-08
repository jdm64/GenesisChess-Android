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

import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class MainMenuPhone extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new MainMenuFrag(), R.layout.activity_basephone);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_mainmenu, menu);
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (resultCode == RESULT_CANCELED)
			return;

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (requestCode == Enums.ONLINE_LIST) {
			startActivity(new Intent(this, GameListOnline.class));
		} else if (requestCode == Enums.USER_STATS) {
			final Intent intent = new Intent(this, UserStats.class);
			intent.putExtra(PrefKey.USERNAME, pref.getString(PrefKey.USERNAME, PrefKey.KEYERROR));
			startActivity(intent);
		}
	}
}
