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

import android.content.*;
import android.os.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import androidx.fragment.app.*;

public class Login extends AbstractPhoneActivity
{
	@Override
	protected Fragment createFrag(Bundle bundle)
	{
		return new LoginFrag();
	}

	@Override
	public void onActivityResult(final int reques, final int result, final Intent data)
	{
		super.onActivityResult(reques, result, data);
		final String username = Pref.getBool(this, R.array.pf_isLoggedIn) ? Pref.getString(this, R.array.pf_username) : "";

		EditText txt = findViewById(R.id.username);
		txt.setText(username);

		txt = findViewById(R.id.password);
		txt.setText("");
	}
}
