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
	public ViewFlip3D(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void flip()
	{
		float centerX = getWidth() / 2.0f;
		float centerY = getHeight() / 2.0f;

		Flip3D animOut = new Flip3D(-90, 0, centerX, centerY);
		animOut.setDuration(500);
		animOut.setInterpolator(new AccelerateInterpolator());
		animOut.setFillAfter(true);

		Flip3D animIn = new Flip3D(0, 90, centerX, centerY);
		animIn.setDuration(500);
		animIn.setInterpolator(new DecelerateInterpolator());
		animIn.setFillAfter(true);

		animIn.setAnimationListener(new ShowNextView(this, animOut));

		ViewGroup view = (ViewGroup) getCurrentView();
		view.startAnimation(animIn);
	}

	private class Flip3D extends Animation
	{
		private final float mFromDegrees;
		private final float mToDegrees;
		private final float mCenterX;
		private final float mCenterY;

		private Camera mCamera;

		public Flip3D(float fromDegrees, float toDegrees, float centerX, float centerY)
		{
			mFromDegrees = fromDegrees;
			mToDegrees = toDegrees;
			mCenterX = centerX;
			mCenterY = centerY;
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight)
		{
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t)
		{
			final float fromDegrees = mFromDegrees;
			float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
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
		private Flip3D flipin;
		private ViewFlip3D container;

		public ShowNextView(ViewFlip3D Container, Flip3D Flipin)
		{
			flipin = Flipin;
			container = Container;
		}

		public void onAnimationRepeat(Animation animation)
		{
		}

		public void onAnimationStart(Animation animation)
		{
		}

		public void onAnimationEnd(Animation animation)
		{
			ViewGroup view = (ViewGroup) container.getCurrentView();

			// Disable clicks
			for (int i = 0; i < view.getChildCount(); i++) {
				View child = view.getChildAt(i);
				child.setVisibility(View.INVISIBLE);
			}

			container.showNext();
			view = (ViewGroup) container.getCurrentView();

			// Enable clicks
			for (int i = 0; i < view.getChildCount(); i++) {
				View child = view.getChildAt(i);
				child.setVisibility(View.VISIBLE);
			}
			view.startAnimation(flipin);
		}
	}
}
