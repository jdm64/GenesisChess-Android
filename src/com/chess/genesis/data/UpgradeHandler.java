package com.chess.genesis;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

class UpgradeHandler
{
	private static Context context;
	private static PackageInfo pinfo;
	private static SharedPreferences pref;

	public static void run(final Context _context)
	{
	try {
		context = _context;
		pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		pref = PreferenceManager.getDefaultSharedPreferences(context);

		upgrade(pref.getInt("appVersion", 0), pinfo.versionCode);
	} catch (NameNotFoundException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	private static void upgrade(final int oldVer, final int newVer)
	{
		if (oldVer == newVer)
			return;

		final Editor edit = pref.edit();

		if (oldVer < 28) {
			edit.putBoolean("isLoggedIn", false);
			edit.putString("username", "!error!");
		}

		edit.putInt("appVersion", newVer);
		edit.commit();
	}
}
