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

package com.chess.genesis;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

class MyImageView extends ImageView
{
	public MyImageView(final Context context)
	{
		super(context);

		// To fix brain-dead non-auto-updating view bounds on images!!!
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	public MyImageView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		// To fix brain-dead non-auto-updating view bounds on images!!!
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
		setMeasuredDimension(width, height);
	}
}
