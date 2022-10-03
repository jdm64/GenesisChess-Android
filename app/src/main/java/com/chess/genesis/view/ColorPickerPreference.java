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

import android.R.id;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.data.*;
import com.chess.genesis.view.ColorPickerDialog.*;
import androidx.fragment.app.*;
import androidx.preference.*;

public class ColorPickerPreference extends Preference implements OnColorChangedListener
{
	private FragmentManager fragMan;
	private ViewGroup container;
	private int size;
	private int border;
	private int intKey;
	private int currColor;

	public ColorPickerPreference(final Context context)
	{
		super(context, null);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
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

	public void init(int prefIntKey, FragmentManager manager)
	{
		intKey = prefIntKey;
		fragMan = manager;
	}

	@Override
	public void onBindViewHolder(final PreferenceViewHolder view)
	{
		super.onBindViewHolder(view);
		container = (ViewGroup) view.findViewById(id.widget_frame);
		container.addView(new ImageView(getContext()));
		container.setVisibility(View.VISIBLE);
		setColor(new Pref(getContext()).getInt(intKey));
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index)
	{
		return a.getColor(index, MColors.WHITE);
	}

	@Override
	public void onClick()
	{
		ColorPickerDialog.create(this, currColor).show(fragMan, "");
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

	private void setColor(final int color)
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
		setColor(getPersistedInt(currColor));
	}
}
