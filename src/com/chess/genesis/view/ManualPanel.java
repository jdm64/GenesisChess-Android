package com.chess.genesis;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.view.View;

class ManualPanel extends LinearLayout
{
	private int totalDivs;
	private int[] sizesArr;

	public ManualPanel(final Context context)
	{
		this(context, null);
	}

	public ManualPanel(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ManualPanel);

		final String[] sizeStr = a.getString(R.styleable.ManualPanel_sizes).split("/");
		final String[] fracArr = sizeStr[0].split(",");
		a.recycle();

		int sum = 0;
		sizesArr = new int[fracArr.length];
		for (int i = 0; i < sizesArr.length; i++) {
			sizesArr[i] = Integer.valueOf(fracArr[i]);
			sum += sizesArr[i];
		}
		totalDivs = Math.max(sum, Integer.valueOf(sizeStr[1]));
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int children = getChildCount();
		final int totalWidth = MeasureSpec.getSize(widthMeasureSpec);

		totalDivs = Math.max(totalDivs, children);

		final int[] arr = IntArray.copyOf(sizesArr, children);
		final int width[] = new int[children];
		int sum = 0;

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
			final LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();

			lp.width = width[i];
			view.setLayoutParams(lp);
		}
	}
}
