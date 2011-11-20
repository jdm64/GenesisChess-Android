package com.chess.genesis;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

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
		Typeface font;

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
