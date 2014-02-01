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
import android.content.res.*;
import android.graphics.*;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import com.chess.genesis.*;
import java.lang.ref.*;

public class MyImageView extends ImageView
{
	private int resId;

	public MyImageView(final Context context)
	{
		super(context);

		// To fix brain-dead non-auto-updating view bounds on images!!!
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);
		resId = 0;
	}

	public MyImageView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		// To fix brain-dead non-auto-updating view bounds on images!!!
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyImageView);
		setResId(a.getResourceId(R.styleable.MyImageView_src, 0));
		a.recycle();
	}

	@Override
	protected void onMeasure(final int widthSpec, final int heightSpec)
	{
		final int imgWidth, imgHeight;
		final Drawable draw = getDrawable();
		if (getDrawable() == null) {
			final Options opt = readImageSize();
			imgWidth = opt.outWidth;
			imgHeight = opt.outHeight;
		} else {
			imgHeight = draw.getIntrinsicHeight();
			imgWidth = draw.getIntrinsicWidth();
		}
		final int width = MeasureSpec.getSize(widthSpec);
		final int height = width * imgHeight / imgWidth;
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		if (getDrawable() == null && resId != 0)
			new ImageLoader(this, getResources()).execute(resId, getMeasuredWidth(), getMeasuredHeight());
		super.onDraw(canvas);
	}

	private BitmapFactory.Options readImageSize()
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), resId, options);
		return options;
	}

	public void setResId(final int id)
	{
		resId = id;
	}

	private static class ImageLoader extends AsyncTask<Integer, Void, Bitmap>
	{
		private final WeakReference<ImageView> viewRef;
		private final Resources res;

		public ImageLoader(final ImageView imageView, final Resources resources)
		{
			viewRef = new WeakReference<ImageView>(imageView);
			res = resources;
		}

		@Override
		protected Bitmap doInBackground(final Integer... params)
		{
			final int id = params[0];
			final int width = params[1];
			final int height = params[2];

			return Bitmap.createScaledBitmap(BitmapFactory.
				decodeResource(res, id), width, height, true);
		}

		@Override
		protected void onPostExecute(final Bitmap bitmap)
		{
			if (viewRef != null && bitmap != null) {
				final ImageView imageView = viewRef.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
}
