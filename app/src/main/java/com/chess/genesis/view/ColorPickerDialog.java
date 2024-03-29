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

import java.util.Map.*;
import android.app.AlertDialog.*;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.os.*;
import android.view.*;
import com.chess.genesis.*;
import com.chess.genesis.util.*;
import androidx.fragment.app.DialogFragment;

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

