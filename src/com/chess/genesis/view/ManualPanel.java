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
import android.content.res.*;
import android.util.*;
import android.view.*;
import com.chess.genesis.*;
import com.chess.genesis.util.*;

public class ManualPanel extends ViewGroup
{
	private int totalDivs;
	private int[] sizesArr;

	public ManualPanel(final Context context)
	{
		super(context);
	}

	public ManualPanel(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ManualPanel);

		setSizes(a.getString(R.styleable.ManualPanel_sizes));
		a.recycle();
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int children = getChildCount();
		final int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		final int[] arr = IntArray.copyOf(sizesArr, children);
		final int[] width = new int[children];
		int sum = 0, totalHeight = 0;

		totalDivs = Math.max(totalDivs, children);

		for (int i = 0; i < children; i++) {
			width[i] = (arr[i] * totalWidth) / totalDivs;
			sum += width[i];
		}
		for (int i = 0, diff = totalWidth - sum; diff > 0; i = ++i % children) {
			width[i]++;
			diff--;
		}
		for (int i = 0; i < children; i++) {
			final View view = getChildAt(i);
			final LayoutParams lp = view.getLayoutParams();

			lp.width = width[i];
			view.setLayoutParams(lp);
			view.measure(MeasureSpec.EXACTLY | lp.width, MeasureSpec.AT_MOST | maxHeight);
			totalHeight = Math.max(totalHeight, view.getMeasuredHeight());
		}
		for (int i = 0; i < children; i++) {
			final View view = getChildAt(i);
			view.measure(MeasureSpec.EXACTLY | width[i], MeasureSpec.EXACTLY | totalHeight);
		}
		setMeasuredDimension(totalWidth, totalHeight);
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b)
	{
		final int children = getChildCount(), pHeight = getMeasuredHeight();

		for (int i = 0, left = l; i < children; i++) {
			final View view = getChildAt(i);

			if (view.getVisibility() != View.VISIBLE)
				continue;

			final int cWidth = view.getMeasuredWidth();

			view.layout(left, 0, left + cWidth, pHeight);
			left += cWidth;
		}
	}

	final public void setSizes(final String str)
	{
		final String[] sizeStr = str.split("/");
		final String[] fracArr = sizeStr[0].split(",");

		int sum = 0;
		sizesArr = new int[fracArr.length];
		for (int i = 0; i < sizesArr.length; i++) {
			sizesArr[i] = Integer.parseInt(fracArr[i]);
			sum += sizesArr[i];
		}
		totalDivs = Math.max(sum, Integer.parseInt(sizeStr[1]));
	}
}
