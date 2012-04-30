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
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class ViewFlip3D extends ViewSwitcher
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

	private static class Flip3D extends Animation
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

	private final static class ShowNextView implements Animation.AnimationListener
	{
		private final Flip3D flipin;
		private final ViewFlip3D container;

		public ShowNextView(final ViewFlip3D Container, final Flip3D Flipin)
		{
			flipin = Flipin;
			container = Container;
		}

		@Override
		public void onAnimationRepeat(final Animation animation)
		{
			// do nothing
		}

		@Override
		public void onAnimationStart(final Animation animation)
		{
			// do nothing
		}

		@Override
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
