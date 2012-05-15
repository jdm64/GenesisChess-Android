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
import android.content.SharedPreferences.Editor;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.*;

public final class UpgradeHandler
{
	private UpgradeHandler()
	{
	}

	public static void run(final Context context)
	{
	try {
		final PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		upgrade(pref, pref.getInt(PrefKey.APPVERSION, 0), pinfo.versionCode);
	} catch (final NameNotFoundException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	private static void upgrade(final SharedPreferences pref, final int oldVer, final int newVer)
	{
		if (oldVer == newVer)
			return;

		final Editor edit = pref.edit();

		if (oldVer < 28) {
			edit.putBoolean(PrefKey.ISLOGGEDIN, false);
			edit.putString(PrefKey.USERNAME, PrefKey.KEYERROR);
		}

		edit.putBoolean(PrefKey.ADS_ON, true);
		edit.putInt(PrefKey.APPVERSION, newVer);
		edit.commit();
	}
}
