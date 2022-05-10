package com.chess.genesis.activity;

import android.os.*;
import com.chess.genesis.R;
import androidx.preference.*;

public class SettingsFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.settings_page, rootKey);
	}
}
