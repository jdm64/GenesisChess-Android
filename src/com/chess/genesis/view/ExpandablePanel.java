package com.chess.genesis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

class ExpandablePanel extends LinearLayout implements OnClickListener
{
	private View top;
	private int duration = 500;
	private int totalHeight = 0;
	private boolean isExpanded = false;
	private Paint paint;

	public ExpandablePanel(final Context context)
	{
		this(context, null);
	}

	public ExpandablePanel(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		paint = new Paint();
		paint.setColor(0xff00b7eb);
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

			final android.view.ViewGroup.LayoutParams lp = child.getLayoutParams();
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

	private class ExpandAnimation extends Animation
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
			final android.view.ViewGroup.LayoutParams lp = item.getLayoutParams();
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
