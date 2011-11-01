package com.chess.genesis;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

class ViewFlip3D extends ViewSwitcher
{
	public ViewFlip3D(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void flip()
	{
		final float centerX = getWidth() / 2.0f;
		final float centerY = getHeight() / 2.0f;

		final Flip3D animOut = new Flip3D(-90, 0, centerX, centerY);
		animOut.setDuration(500);
		animOut.setInterpolator(new AccelerateInterpolator());
		animOut.setFillAfter(true);

		final Flip3D animIn = new Flip3D(0, 90, centerX, centerY);
		animIn.setDuration(500);
		animIn.setInterpolator(new DecelerateInterpolator());
		animIn.setFillAfter(true);

		animIn.setAnimationListener(new ShowNextView(this, animOut));

		final ViewGroup view = (ViewGroup) getCurrentView();
		view.startAnimation(animIn);
	}

	private class Flip3D extends Animation
	{
		private final float mFromDegrees;
		private final float mToDegrees;
		private final float mCenterX;
		private final float mCenterY;

		private Camera mCamera;

		public Flip3D(final float fromDegrees, final float toDegrees, final float centerX, final float centerY)
		{
			super();

			mFromDegrees = fromDegrees;
			mToDegrees = toDegrees;
			mCenterX = centerX;
			mCenterY = centerY;
		}

		@Override
		public void initialize(final int width, final int height, final int parentWidth, final int parentHeight)
		{
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(final float interpolatedTime, final Transformation t)
		{
			final float fromDegrees = mFromDegrees;
			final float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
			final float centerX = mCenterX;
			final float centerY = mCenterY;
			final Camera camera = mCamera;

			final Matrix matrix = t.getMatrix();

			camera.save();

			camera.rotateY(degrees);

			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}

	private final class ShowNextView implements Animation.AnimationListener
	{
		private final Flip3D flipin;
		private final ViewFlip3D container;

		public ShowNextView(final ViewFlip3D Container, final Flip3D Flipin)
		{
			flipin = Flipin;
			container = Container;
		}

		public void onAnimationRepeat(final Animation animation)
		{
		}

		public void onAnimationStart(final Animation animation)
		{
		}

		public void onAnimationEnd(final Animation animation)
		{
			ViewGroup view = (ViewGroup) container.getCurrentView();

			// Disable clicks
			for (int i = 0; i < view.getChildCount(); i++) {
				final View child = view.getChildAt(i);
				child.setVisibility(View.INVISIBLE);
			}

			container.showNext();
			view = (ViewGroup) container.getCurrentView();

			// Enable clicks
			for (int i = 0; i < view.getChildCount(); i++) {
				final View child = view.getChildAt(i);
				child.setVisibility(View.VISIBLE);
			}
			view.startAnimation(flipin);
		}
	}
}