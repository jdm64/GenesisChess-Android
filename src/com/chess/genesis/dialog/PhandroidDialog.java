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
import com.chess.genesis.*;

public class PhandroidDialog extends BaseDialog
{
	public PhandroidDialog(final Context context)
	{
		super(context, CANCEL);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("We Win, You Win!");
		setBodyView(R.layout.dialog_phandroid);
		setButtonTxt(R.id.cancel, "Go Up Vote The Comment");
	}

	@Override
	public void onClick(final View v)
	{
		final Uri uri = Uri.parse("http://phandroid.com/2012/06/26/contest-decide-who-wins-google-io-gift-pack-2/#comment-567846811");
		final Editor edit = PreferenceManager.getDefaultSharedPreferences(v.getContext()).edit();
		edit.putBoolean("show_phandroid", false);
		edit.commit();
		dismiss();

		v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}
}
