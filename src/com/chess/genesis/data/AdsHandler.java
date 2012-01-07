package com.chess.genesis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

final class AdsHandler
{
	private AdsHandler()
	{
	}

	public static void run(final Activity activity)
	{
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);

		if (!pref.getBoolean("enableAds", true))
			return;
		final AdView ad = (AdView) activity.findViewById(R.id.adView);
		ad.loadAd(new AdRequest());
	}
}
