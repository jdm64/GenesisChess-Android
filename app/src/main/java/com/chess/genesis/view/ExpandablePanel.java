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
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import com.chess.genesis.data.*;

public class ExpandablePanel extends LinearLayout implements OnClickListener
{
	private View top;
	private final static int duration = 500;
	private int totalHeight = 0;
	private boolean isExpanded = false;
	private final Paint paint;

	public ExpandablePanel(final Context context)
	{
		this(context, null);
	}

	public ExpandablePanel(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		paint = new Paint();
		paint.setColor(MColors.BLUE_LIGHT_500);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		final int count = getChildCount();

		if (count < 1)
			return;

		top = getChildAt(0);
		top.measure(getWidth(), MeasureSpec.UNSPECIFIED);
		totalHeight = top.getMeasuredHeight();

		if (count < 2)
			return;

		for (int i = 1; i < count; i++) {
			final View child = getChildAt(i);

			child.measure(getWidth(), MeasureSpec.UNSPECIFIED);
			totalHeight += child.getMeasuredHeight();

			final ViewGroup.LayoutParams lp = child.getLayoutParams();
			lp.height = 0;

			child.setLayoutParams(lp);
			child.setVisibility(View.GONE);
		}
		setOnClickListener(this);
	}

	@Override
	public void dispatchDraw(final Canvas canvas)
	{
		super.dispatchDraw(canvas);

		canvas.drawRect(0, getHeight() - 4, getWidth(), getHeight(), paint);
	}

	@Override
	public void onClick(final View v)
	{
		if (isExpanded) {
			for (int i = 1; i < getChildCount(); i++) {
				final View item = getChildAt(i);

				final ExpandAnimation a = new ExpandAnimation(item, item.getHeight(), 0);
				a.setDuration(duration);
				item.startAnimation(a);
			}
			final ExpandAnimation a = new ExpandAnimation(this, getHeight(), top.getHeight());
			a.setDuration(duration);
			startAnimation(a);
		} else {
			final ExpandAnimation b = new ExpandAnimation(this, getHeight(), totalHeight);
			b.setDuration(duration);
			startAnimation(b);

			for (int i = getChildCount() - 1; i > 0; i--) {
				final View item = getChildAt(i);
				item.measure(getWidth(), MeasureSpec.UNSPECIFIED);
				final int height = item.getMeasuredHeight();

				final ExpandAnimation a = new ExpandAnimation(item, 0, height);
				a.setDuration(duration);
				item.startAnimation(a);
			}
		}
		isExpanded ^= true;
	}

	private static class ExpandAnimation extends Animation
	{
		private final View item;
		private final int start;
		private final int delta;

		public ExpandAnimation(final View view, final int startHeight, final int endHeight)
		{
			item = view;
			item.setVisibility(View.VISIBLE);

			start = startHeight;
			delta = endHeight - start;
		}

		@Override
		protected void applyTransformation(final float interpolatedTime, final Transformation t)
		{
			final ViewGroup.LayoutParams lp = item.getLayoutParams();
			lp.height = (int) (start + delta * interpolatedTime);

			item.setLayoutParams(lp);
		}

		@Override
		public boolean willChangeBounds()
		{
			return true;
		}
	}
}
