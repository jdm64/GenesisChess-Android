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

package com.chess.genesis.dialog;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class GenesisRulesDialog extends BaseDialog
{
	public GenesisRulesDialog(final Context context)
	{
		super(context);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("Genesis Chess Rules!");
		setBodyView(R.layout.dialog_genesis_rules);
		setButtonTxt(R.id.cancel, "Close");
		setButtonTxt(R.id.ok, "Full Rules");
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final Uri uri = Uri.parse("http://genesischess.com");
			v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
		} else {
			final CheckBox agree = (CheckBox) findViewById(R.id.agree);
			final Editor edit = PreferenceManager.getDefaultSharedPreferences(v.getContext()).edit();

			edit.putBoolean(PrefKey.SHOW_GENESIS_RULES, !agree.isChecked());
			edit.commit();

			dismiss();
		}
	}
}
