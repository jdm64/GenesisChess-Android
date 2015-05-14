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
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.dialog.ColorPickerDialog.OnColorChangedListener;

public class ColorPickerPreference extends Preference implements OnPreferenceClickListener, OnColorChangedListener
{
	private ViewGroup container;
	private int size;
	private int border;
	private int currColor;

	public ColorPickerPreference(final Context context)
	{
		super(context, null);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setOnPreferenceClickListener(this);
		final float density = (int) getContext().getResources().getDisplayMetrics().density;
		size = (int) (40 * density);
		border = (int) (0.05 * size);
	}

	private static int antiGrey(final int color)
	{
		final int total = Color.red(color) + Color.green(color) + Color.blue(color);
		final int anti = (765 - total) / 6;
		return Color.argb(0xff, anti, anti, anti);
	}

	@Override
	public void onBindView(final View view)
	{
		super.onBindView(view);
		container = (ViewGroup) view.findViewById(android.R.id.widget_frame);
		container.addView(new ImageView(getContext()));
		container.setVisibility(View.VISIBLE);
		setColor(currColor);
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index)
	{
		return a.getColor(index, MColors.WHITE);
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue)
	{
		onColorChanged(restoreValue? getPersistedInt(currColor) : (Integer) defaultValue);
	}

	@Override
	public boolean onPreferenceClick(final Preference pref)
	{
		new ColorPickerDialog(getContext(), this, currColor).show();
		return false;
	}

	@Override
	public void onColorChanged(final int color)
	{
		if (isPersistent())
			persistInt(color);
		setColor(color);

		// update colors
		PieceImgPainter.setColors(getContext());

		final OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
		if (listener != null)
			listener.onPreferenceChange(this, color);
	}

	public void setColor(final int color)
	{
		currColor = color;
		if (container == null)
			return;

		final Bitmap bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bm);
		final Paint paint = new Paint();
		paint.setColor(antiGrey(currColor));
		canvas.drawRect(new Rect(0, 0, size, size), paint);
		paint.setColor(currColor);
		canvas.drawRect(new Rect(border, border, size - border, size - border), paint);

		final ImageView img = (ImageView) container.getChildAt(0);
		img.setImageBitmap(bm);
	}

	public void update()
	{
		setColor(getPersistedInt(MColors.WHITE));
	}
}
