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

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

class RobotoEditText extends EditText
{
	public RobotoEditText(final Context context)
	{
		this(context, null);
	}

	public RobotoEditText(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void setTypeface(final Typeface tf, final int style)
	{
		final Typeface font;

		switch (style) {
		default:
		case 0:
			font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
			break;
		case 1:
			font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
			break;
		case 2:
			font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Italic.ttf");
			break;
		case 3:
			font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-BoldItalic.ttf");
			break;
		}
		super.setTypeface(font);
	}

	public void setTypeface(final Typeface tf)
	{
		setTypeface(tf, tf.getStyle());
	}
}
