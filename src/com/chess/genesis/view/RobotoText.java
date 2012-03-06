package com.chess.genesis;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

class RobotoText extends TextView
{
	public RobotoText(final Context context)
	{
		this(context, null);
	}

	public RobotoText(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	public static float maxTextWidth(final String[] list, final TextPaint paint, final float width)
	{
		int n = 0; float txtWidth = 0;
		for (int i = 0; i < list.length; i++) {
			final float tmp = paint.measureText(list[i]);

			if (tmp > txtWidth) {
				txtWidth = tmp;
				n = i;
			}
		}

		float newSize = 0;
		for (int i = 0; i < 2; i++) {
			newSize = paint.getTextSize() * (width / paint.measureText(list[n]));
			paint.setTextSize(newSize);
		}
		return newSize;
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
