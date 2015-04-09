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

package com.chess.genesis.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import com.chess.genesis.data.*;

public class TabText extends RobotoText implements OnClickListener, OnTouchListener
{
	private final static int highlightColor = MColors.BLUE_LIGHT_500;
	private final static int touchColor = MColors.BLUE_LIGHT_500_TR;

	private final int THIN = dptopx(2);
	private final int THICK = dptopx(5);

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

	private int dptopx(final int dp)
	{
		final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		return (int) ((dp * displayMetrics.density) + 0.5);
	}

	@Override
	public void onDraw(final Canvas canvas)
	{
		super.onDraw(canvas);

		final int barSize = active? THICK : THIN;
		canvas.drawRect(0, getHeight() - barSize, getWidth(), getHeight(), paint);
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event)
	{
		setBackgroundColor((event.getAction() == MotionEvent.ACTION_DOWN)? touchColor : MColors.CLEAR);
		return false;
	}

	@Override
	public void onClick(final View v)
	{
		// do nothing
	}

	public void setActive(final boolean Active)
	{
		active = Active;
		setTextColor(active? Color.WHITE : Color.GRAY);
	}

	public void setTabTextColor(final int color)
	{
		setTextColor(color);
	}
}
