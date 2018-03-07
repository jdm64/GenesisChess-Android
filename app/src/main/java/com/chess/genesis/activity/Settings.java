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

import android.app.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import com.chess.genesis.R;

public class Settings extends Activity implements View.OnLongClickListener
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_basephone);

		final View image = findViewById(R.id.topbar_genesis);
		image.setOnLongClickListener(this);

		getFragmentManager().beginTransaction()
			.replace(R.id.fragment01, new SettingsFrag())
			.commit();
	}

	@Override
	public boolean onLongClick(final View v)
	{
		if (v.getId() == R.id.topbar_genesis) {
			finish();
			return true;
		}
		return false;
	}
}
