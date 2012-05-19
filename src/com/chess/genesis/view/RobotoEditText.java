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

package com.chess.genesis.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.widget.*;

public class RobotoEditText extends EditText
{
	public RobotoEditText(final Context context)
	{
		this(context, null);
	}

	public RobotoEditText(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void setTypeface(final Typeface tf, final int style)
	{
		super.setTypeface(RobotoText.getRobotoFont(getContext().getAssets(), style));
	}

	@Override
	public void setTypeface(final Typeface tf)
	{
		setTypeface(tf, tf.getStyle());
	}
}
