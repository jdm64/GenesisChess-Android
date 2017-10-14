/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.dialog;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.chess.genesis.*;

abstract class BaseDialog extends Dialog implements OnClickListener
{
	public final static int CANCEL = 1;
	public final static int OKCANCEL = 3;

	private final int buttonCount;

	public BaseDialog(final Context context)
	{
		super(context);
		buttonCount = OKCANCEL;
	}

	public BaseDialog(final Context context, final int ButtonCount)
	{
		super(context);
		buttonCount = ButtonCount;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		View button;

		if (buttonCount == OKCANCEL) {
			setContentView(R.layout.dialog_base_okcancel);

			button = findViewById(R.id.ok);
			button.setOnClickListener(this);
		} else {
			setContentView(R.layout.dialog_base_cancel);
		}
		button = findViewById(R.id.cancel);
		button.setOnClickListener(this);
	}

	public void setBodyView(final int layoutID)
	{
		final ViewGroup gView = findViewById(R.id.body);
		getLayoutInflater().inflate(layoutID, gView, true);
	}

	public void setButtonTxt(final int buttonId, final String txt)
	{
		final Button button = findViewById(buttonId);

		if (button == null)
			return;
		button.setText(txt);
	}
}
