/* GenesisChess, an Android chess application
 * Copyright 2022, Justin Madru (justin.jdm64@gmail.com)
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
import com.chess.genesis.R;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;
import androidx.preference.*;
import androidx.preference.Preference.*;

public class SettingsFragment extends PreferenceFragmentCompat implements
    OnPreferenceClickListener, CallBackPreference.CallBack
{
	private final static int[] colorKeys = new int[]{R.array.pf_bcInnerCheck, R.array.pf_bcInnerDark,
	    R.array.pf_bcInnerLast, R.array.pf_bcInnerLight, R.array.pf_bcInnerSelect,
	    R.array.pf_bcOuterDark, R.array.pf_bcOuterLight };

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.settings_page, rootKey);

		var prefView = findPreference("benchmark");
		prefView.setOnPreferenceClickListener(this);

		var pref = new Pref(getContext());
		for (var key : colorKeys) {
			ColorPickerPreference picker = findPreference(pref.key(key));
			picker.init(key, getParentFragmentManager());
		}

		CallBackPreference callbackPref = findPreference("bcReset");
		callbackPref.setCallBack(this);
	}

	@Override
	public void runCallBack(CallBackPreference preference)
	{
		final String key = preference.getKey();

		switch (key) {
		case "bcReset":
			resetBoardColors();
			break;
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		var key = preference.getKey();

		if (key.equals("benchmark"))
			BenchmarkDialog.create().show(getParentFragmentManager(), "");
		return true;
	}

	private void resetBoardColors()
	{
		PieceImgPainter.resetColors(getContext());

		var pref = new Pref(getContext());
		for (var key : colorKeys) {
			ColorPickerPreference colorPref = findPreference(pref.key(key));
			colorPref.update();
		}
	}
}
