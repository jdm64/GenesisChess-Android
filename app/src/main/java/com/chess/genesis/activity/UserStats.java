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
import com.chess.genesis.*;

public class UserStats extends BasePhoneActivity
{
	@Override
	protected BaseContentFrag createFrag(Bundle bundle)
	{
		return new UserStatsFrag();
	}

	@Override
	protected int getLayoutId(Bundle bundle)
	{
		return R.layout.activity_basephone;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_userstats, menu);
		return true;
	}
}
