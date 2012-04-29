/* source: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/widget/NumberPickerButton.java;hb=HEAD
 * SICK! Added in API 11, renamed from NumberPickerButton to NumberSpinnerButton
 *
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.view;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;

/**
 * This class exists purely to cancel long click events, that got
 * started in NumberPicker
 */
class NumberSpinnerButton extends ImageView
{
	private NumberSpinner mNumberSpinner;

	public NumberSpinnerButton(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	public NumberSpinnerButton(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	public NumberSpinnerButton(final Context context)
	{
		super(context);
		setAdjustViewBounds(true);
		setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	public void setNumberSpinner(final NumberSpinner picker)
	{
		mNumberSpinner = picker;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		cancelLongpressIfRequired(event);
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event)
	{
		cancelLongpressIfRequired(event);
		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
			cancelLongpress();
		}
		return super.onKeyUp(keyCode, event);
	}

	private void cancelLongpressIfRequired(final MotionEvent event) 
	{
		if ((event.getAction() == MotionEvent.ACTION_CANCEL) || (event.getAction() == MotionEvent.ACTION_UP)) {
			cancelLongpress();
		}
	}

	private void cancelLongpress()
	{
		if (R.id.increment == getId()) {
			mNumberSpinner.cancelIncrement();
		} else if (R.id.decrement == getId()) {
			mNumberSpinner.cancelDecrement();
		}
	}

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			cancelLongpress();
		}
	}
}
