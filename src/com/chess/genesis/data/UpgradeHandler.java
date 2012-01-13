package com.chess.genesis;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

final class UpgradeHandler
{
	private UpgradeHandler()
	{
	}

	public static void run(final Context context)
	{
	try {
		final PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		upgrade(pref, pref.getInt("appVersion", 0), pinfo.versionCode);
	} catch (NameNotFoundException e) {
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
			edit.putBoolean("isLoggedIn", false);
			edit.putString("username", "!error!");
		}

		edit.putBoolean("enableAds", true);
		edit.putInt("appVersion", newVer);
		edit.commit();
	}
}
