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

		if (ad != null)
			ad.loadAd(new AdRequest());
	}
}
