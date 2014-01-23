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

package com.chess.genesis.data;

import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import com.chess.genesis.*;

public final class UpgradeHandler
{
	private UpgradeHandler()
	{
	}

	public static void run(final Context context)
	{
	try {
		final PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

		upgrade(context, Pref.getInt(context, R.array.pf_appVersion), pinfo.versionCode);
	} catch (final NameNotFoundException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	private static void upgrade(final Context context, final int oldVer, final int newVer)
	{
		if (oldVer == newVer)
			return;

		final PrefEdit pref = new PrefEdit(context);

		if (oldVer < 28) {
			pref.putBool(R.array.pf_isLoggedIn);
			pref.putString(R.array.pf_username);
		}

		pref.putBool(R.array.pf_enableAds);
		pref.putInt(R.array.pf_appVersion, newVer);
		pref.commit();
	}
}
