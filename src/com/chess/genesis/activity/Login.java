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

package com.chess.genesis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.EditText;

public class Login extends BasePhoneActivity
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, new LoginFrag(), LoginFrag.TAG, R.layout.activity_basephone);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_login, menu);
		return true;
	}

	public void onActivityResult(final int reques, final int result, final Intent data)
	{
		String username = "";
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (pref.getBoolean("isLoggedIn", false))
			username = pref.getString("username", "");

		EditText txt = (EditText) findViewById(R.id.username);
		txt.setText(username);

		txt = (EditText) findViewById(R.id.password);
		txt.setText("");
	}
}
