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

import android.content.pm.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.chess.genesis.*;

public abstract class AbstractPhoneActivity extends AppCompatActivity
{
	protected Fragment frag;

	protected abstract Fragment createFrag(Bundle bundle);

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_abstract_phone);
		setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setLogo(R.drawable.genesischess);

		Bundle settings = (savedInstanceState != null)? savedInstanceState : getIntent().getExtras();
		settings = settings != null? settings : new Bundle();

		Fragment frag = createFrag(settings);
		frag.setArguments(settings);
		getSupportFragmentManager().beginTransaction()
		    .replace(R.id.fragment01, frag, frag.getClass().getName()).commit();
	}
}