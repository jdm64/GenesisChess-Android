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
import android.util.*;
import android.view.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;

public class ManualPanel extends ViewGroup
{
	// child height policies
	private final static int EXACT_CHILD = 0; // children are size they want to be
	private final static int LARGEST_CHILD = 1; // all children set to size of largest child
	private final static int MATCH_PARENT = 2; // child height matches parent

	private int totalDivs;
	private int[] sizesArr;
	private int heightPolicy = EXACT_CHILD;

	public ManualPanel(final Context context)
	{
		super(context);
	}

	public ManualPanel(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setLayout(context, attrs);
	}

	private void setLayout(Context context, AttributeSet attrs)
	{
		if (attrs == null) {
			return;
		}

		var a = context.obtainStyledAttributes(attrs, R.styleable.ManualPanel);
		var hp = a.getString(R.styleable.ManualPanel_height_policy);

		if (hp != null) {
			if (hp.equals("match_parent"))
				heightPolicy = MATCH_PARENT;
			else if (hp.equals("largest_child"))
				heightPolicy = LARGEST_CHILD;
		}
		setSizes(a.getString(R.styleable.ManualPanel_sizes));
		a.recycle();
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int children = getChildCount();
		final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

		if (children == 0) {
			setMeasuredDimension(maxWidth, maxHeight);
			return;
		}
		final int[] arr = IntArray.clone(sizesArr, children);
		final int[] width = new int[children];
		int sum = 0, totalHeight = 0;

		totalDivs = Math.max(totalDivs, children);

		for (int i = 0; i < children; i++) {
			width[i] = (((arr[i] != 0)? arr[i] : 1) * maxWidth) / totalDivs;
			sum += width[i];
		}
		for (int i = 0, diff = maxWidth - sum; diff > 0; i = ++i % children) {
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
		if (heightPolicy != EXACT_CHILD) {
			final int setHeight = (heightPolicy == MATCH_PARENT)? maxHeight : totalHeight;
			for (int i = 0; i < children; i++) {
				final View view = getChildAt(i);
				view.measure(MeasureSpec.EXACTLY | width[i], MeasureSpec.EXACTLY | setHeight);
			}
		}
		setMeasuredDimension(maxWidth, (heightPolicy == MATCH_PARENT)? maxHeight : totalHeight);
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b)
	{
		final int children = getChildCount();

		for (int i = 0, left = l; i < children; i++) {
			final View view = getChildAt(i);

			if (view.getVisibility() != View.VISIBLE)
				continue;

			final int cWidth = view.getMeasuredWidth();
			final int cHeight = view.getMeasuredHeight();

			view.layout(left, 0, left + cWidth, cHeight);
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
