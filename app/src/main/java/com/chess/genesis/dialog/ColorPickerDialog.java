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

package com.chess.genesis.dialog;

import java.util.Map.*;
import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.os.*;
import android.util.*;
import android.view.*;
import com.chess.genesis.*;

public class ColorPickerDialog extends DialogFragment implements OnClickListener
{
	public interface OnColorChangedListener
	{
		void onColorChanged(int color);
	}

	private OnColorChangedListener callback;
	private ColorPicker colorPicker;
	private int color;

	public static ColorPickerDialog create(OnColorChangedListener listener, int initColor)
	{
		ColorPickerDialog dialog = new ColorPickerDialog();
		dialog.callback = listener;
		dialog.color = initColor;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle bundle)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_base);

		builder.getValue()
			.setTitle("Pick Color")
			.setPositiveButton("Save", this)
			.setNegativeButton("Cancel", this);

		colorPicker = new ColorPicker(getActivity(), null);
		colorPicker.setColor(color);
		((ViewGroup) builder.getKey()).addView(colorPicker);

		return builder.getValue().create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (DialogInterface.BUTTON_POSITIVE == which) {
			callback.onColorChanged(colorPicker.getColor());
		}
		dismiss();
	}
}

class ColorPicker extends View
{
	private static final int[] COLORS = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00,
		0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};

	enum TouchType {WHEEL, SAT_BAR, VAL_BAR, NONE}

	private final static float WHEEL_SCALE = (float) 0.85;
	private final static float WHEEL_THICKNESS = (float) 0.05;
	private final static float CURRENT_SCALE = (float) 0.40;
	private final static float POINTER_SCALE = (float) 0.10;
	private final static float SHADOW_SCALE = (float) 1.15;
	private final static int SHADOW_COL = 0x88000000;

	private double hue;
	private double sat;
	private double val;

	private TouchType touchType;
	private final double[] lastTouch = new double[2];
	private final float[] hueLoc = new float[2];
	private final float[] satLoc = new float[2];
	private final float[] valLoc = new float[2];

	private final Paint wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint satBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint valBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private float wheelRadius;
	private float pointerRadius;
	private float currentRadius;
	private float offset;

	public ColorPicker(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		wheelPaint.setShader(new SweepGradient(0, 0, COLORS, null));
		wheelPaint.setStyle(Style.STROKE);
		satBarPaint.setStyle(Paint.Style.STROKE);
		valBarPaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(size, (int) ((1 + CURRENT_SCALE - POINTER_SCALE) * size));

		offset = size / 2;
		wheelRadius = WHEEL_SCALE * offset;
		currentRadius = CURRENT_SCALE * offset;
		pointerRadius = Math.min(offset - wheelRadius, POINTER_SCALE * offset);

		satLoc[1] = wheelRadius + 3 * pointerRadius;
		valLoc[1] = satLoc[1] + 3 * pointerRadius;

		wheelPaint.setStrokeWidth(WHEEL_THICKNESS * offset);
		satBarPaint.setStrokeWidth(2 * wheelPaint.getStrokeWidth() / 3);
		valBarPaint.setStrokeWidth(2 * wheelPaint.getStrokeWidth() / 3);

		update();
	}

	@Override
	public void onDraw(final Canvas canvas)
	{
		canvas.translate(offset, offset);
		canvas.drawCircle(0, 0, wheelRadius, wheelPaint);

		canvas.drawLine(-wheelRadius, satLoc[1], wheelRadius, satLoc[1], satBarPaint);
		canvas.drawLine(-wheelRadius, valLoc[1], wheelRadius, valLoc[1], valBarPaint);

		paint.setColor(SHADOW_COL);
		canvas.drawCircle(0, 0, (SHADOW_SCALE - POINTER_SCALE) * currentRadius, paint);
		canvas.drawCircle(hueLoc[0], hueLoc[1], SHADOW_SCALE * pointerRadius, paint);
		canvas.drawCircle(satLoc[0], satLoc[1], SHADOW_SCALE * pointerRadius, paint);
		canvas.drawCircle(valLoc[0], valLoc[1], SHADOW_SCALE * pointerRadius, paint);

		paint.setColor(hsv2Color(hue, 1.0, 1.0));
		canvas.drawCircle(hueLoc[0], hueLoc[1], pointerRadius, paint);

		paint.setColor(getColor());
		canvas.drawCircle(0, 0, currentRadius, paint);
		canvas.drawCircle(satLoc[0], satLoc[1], pointerRadius, paint);
		canvas.drawCircle(valLoc[0], valLoc[1], pointerRadius, paint);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			getParent().requestDisallowInterceptTouchEvent(true);
			setTouchType(event);
			break;
		case MotionEvent.ACTION_MOVE:
			setMove(event);
			break;
		case MotionEvent.ACTION_UP:
			getParent().requestDisallowInterceptTouchEvent(false);
			break;
		}
		return true;
	}

	private void setTouchType(final MotionEvent event)
	{
		final double x = event.getX();
		final double y = event.getY();

		final double radius = getRadius(x - offset, y - offset);
		if (radius <= wheelRadius + pointerRadius && radius >= currentRadius)
			touchType = TouchType.WHEEL;
		else if (Math.abs(y - satLoc[1] - offset) <= pointerRadius)
			touchType = TouchType.SAT_BAR;
		else if (Math.abs(y - valLoc[1] - offset) <= pointerRadius)
			touchType = TouchType.VAL_BAR;
		else
			touchType = TouchType.NONE;
		lastTouch[0] = x;
		lastTouch[1] = y;
	}

	private void setMove(final MotionEvent event)
	{
		final float x = event.getX();
		final float y = event.getY();

		if (lastTouch[0] == x && lastTouch[1] == y)
			return;
		switch (touchType) {
		case WHEEL:
			hue = angleDiff(x, y, hue);
			break;
		case SAT_BAR:
			sat = barDiff(x, sat);
			break;
		case VAL_BAR:
			val = barDiff(x, val);
			break;
		case NONE:
			return;
		}
		lastTouch[0] = x;
		lastTouch[1] = y;
		update();
		invalidate();
	}

	public int getColor()
	{
		return hsv2Color(hue, sat, val);
	}

	public void setColor(final int color)
	{
		final float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hue = hsv[0];
		sat = hsv[1];
		val = hsv[2];
		update();
	}

	private void update()
	{
		satBarPaint.setShader(new LinearGradient(-wheelRadius, satLoc[1],
			wheelRadius, satLoc[1], hsv2Color(hue, 0, val), hsv2Color(hue, 1, val), Shader.TileMode.CLAMP));
		valBarPaint.setShader(new LinearGradient(-wheelRadius, valLoc[1],
			wheelRadius, valLoc[1], Color.BLACK, hsv2Color(hue, sat, 1), Shader.TileMode.CLAMP));
		getXY(wheelRadius, hue, hueLoc);
		satLoc[0] = barXLoc(sat);
		valLoc[0] = barXLoc(val);
	}

	private double angleDiff(final double x, final double y, final double angle)
	{
		return mod(angle + angleDiff(lastTouch[0], lastTouch[1], x, y, offset), 360);
	}

	private float barXLoc(final double percent)
	{
		return (float) (wheelRadius * (2 * percent - 1));
	}

	private double barDiff(final double x, final double percent)
	{
		return clamp(percent + (x - lastTouch[0]) / (2 * wheelRadius));
	}

	private static int hsv2Color(final double h, final double s, final double v)
	{
		return Color.HSVToColor(new float[]{(float) h, (float) s, (float) v});
	}

	private static double clamp(final double n)
	{
		return n < 0? 0 : (n > 1? 1 : n);
	}

	private static double mod(final double v, final double r)
	{
		double m = Math.IEEEremainder(v, r);
		if (m < 0)
			m += r;
		return m;
	}

	private static double angleDiff(final double x1, final double y1, final double x2, final double y2, final double ofs)
	{
		return getAngle(x2 - ofs, y2 - ofs) - getAngle(x1 - ofs, y1 - ofs);
	}

	private static double getAngle(final double x, final double y)
	{
		return mod(Math.toDegrees(Math.atan2(y, x)), 360);
	}

	private static double getRadius(final double x, final double y)
	{
		return Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0));
	}

	private static void getXY(final double r, final double a, final float[] arr)
	{
		final double d = Math.toRadians(a);
		arr[0] = (float) (r * Math.cos(d));
		arr[1] = (float) (r * Math.sin(d));
	}
}
