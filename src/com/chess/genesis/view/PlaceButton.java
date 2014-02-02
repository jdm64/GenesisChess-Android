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
import com.chess.genesis.*;
import com.chess.genesis.data.*;

public class PlaceButton extends PieceImg
{
	public static int outerLight;
	public static int outerDark;
	public static int innerDark;
	public static int innerLight;
	public static int innerSelect;
	public static int innerCheck;
	public static int innerLast;
	public static boolean colorsSet = false;

	protected final static int[] typeCounts = {0, 8, 2, 2, 2, 1, 1};

	protected final Paint paint = new Paint();
	protected final RectF inSquare = new RectF();
	protected final RectF outSquare = new RectF();

	public PlaceButton(final Context context, final PieceImgCache _cache, final int Type)
	{
		super(context, _cache);
		type = Type;
		count = typeCounts[Math.abs(type)];
		setId(type + 1000);
	}

	public static void initColors(final Context context)
	{
		if (colorsSet)
			return;
		setColors(context);
	}

	public static void setColors(final Context context)
	{
		final Pref pref = new Pref(context);
		outerLight = pref.getInt(R.array.pf_bcOuterLight);
		outerDark = pref.getInt(R.array.pf_bcOuterDark);
		innerDark = pref.getInt(R.array.pf_bcInnerDark);
		innerLight = pref.getInt(R.array.pf_bcInnerLight);
		innerSelect = pref.getInt(R.array.pf_bcInnerSelect);
		innerCheck = pref.getInt(R.array.pf_bcInnerCheck);
		innerLast = pref.getInt(R.array.pf_bcInnerLast);
		colorsSet = true;
	}

	public static void resetColors(final Context context)
	{
		final PrefEdit pref = new PrefEdit(context);
		pref.putInt(R.array.pf_bcInnerDark);
		pref.putInt(R.array.pf_bcInnerLight);
		pref.putInt(R.array.pf_bcOuterDark);
		pref.putInt(R.array.pf_bcOuterLight);
		pref.putInt(R.array.pf_bcInnerSelect);
		pref.putInt(R.array.pf_bcInnerCheck);
		pref.putInt(R.array.pf_bcInnerLast);
		pref.commit();
		colorsSet = false;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int size = getMeasuredHeight();
		inSquare.set((float)(size * 0.09), (float)(size * 0.09), (float)(size * 0.91), (float)(size * 0.91));
		outSquare.set(0, 0, size, size);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		drawBackground(canvas);
		super.onDraw(canvas);
	}

	protected void drawBackground(final Canvas canvas)
	{
		// Draw outer square
		if (type % 2 == 0)
			paint.setColor(outerLight);
		else
			paint.setColor(outerDark);
		canvas.drawRect(outSquare, paint);

		// Draw inner square
		final int innerColor =
			isHighlighted?
				innerSelect :
			((type % 2 == 0)?
				innerLight :
				innerDark);
		paint.setColor(innerColor);
		canvas.drawRect(inSquare, paint);
	}

	public void reset()
	{
		isHighlighted = false;
		count = typeCounts[Math.abs(type)];
		invalidate();
	}
}
