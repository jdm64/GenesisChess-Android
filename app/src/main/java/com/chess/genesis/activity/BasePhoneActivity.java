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

import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.view.View.OnLongClickListener;
import com.chess.genesis.*;

abstract class BasePhoneActivity extends BaseActivity implements OnLongClickListener
{
	protected BaseContentFrag mainFrag;

	public void onCreate(final Bundle savedInstanceState, final BaseContentFrag Frag, final int layoutId)
	{
		onCreate(savedInstanceState);
		mainFrag = Frag;

		// set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(layoutId);

		final View image = findViewById(R.id.topbar_genesis);
		image.setOnLongClickListener(this);

		final Bundle settings = (savedInstanceState != null)?
			savedInstanceState : getIntent().getExtras();

		mainFrag.setArguments(settings != null? settings : new Bundle());
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.fragment01, mainFrag, mainFrag.getBTag()).commit();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		return mainFrag.onOptionsItemSelected(item);
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
