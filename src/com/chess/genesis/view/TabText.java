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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

class TabText extends RobotoText implements OnClickListener, OnTouchListener
{
	private final static int highlightColor = 0xff00b7eb;
	private final static int touchColor = 0x8000b7eb;

	private final Paint paint;
	private boolean active;

	public TabText(final Context context)
	{
		this(context, null);
	}

	public TabText(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		active = false;
		paint = new Paint();
		paint.setColor(highlightColor);

		setLines(1);
		setOnTouchListener(this);
		setOnClickListener(this);
	}

	@Override
	public void onDraw(final Canvas canvas)
	{
		super.onDraw(canvas);

		final int barSize = active? 6 : 2;
		canvas.drawRect(0, getHeight() - barSize, getWidth(), getHeight(), paint);
	}

	public boolean onTouch(final View v, final MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			setBackgroundColor(touchColor);
		else if (event.getAction() == MotionEvent.ACTION_UP)
			setBackgroundColor(0x00ffffff);
		return false;
	}

	public void onClick(final View v)
	{
	}

	public void setActive(final boolean Active)
	{
		active = Active;
		setTextColor(active? 0xffffffff : 0xff808080);
	}

	public void setTabTextColor(final int color)
	{
		setTextColor(color);
	}
}
