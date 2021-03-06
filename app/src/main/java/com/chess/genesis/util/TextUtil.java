/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.util;

import android.text.*;

public class TextUtil
{
	public static float maxTextWidth(final String[] list, final TextPaint paint, final float width)
	{
		int n = 0; float txtWidth = 0;
		for (int i = 0; i < list.length; i++) {
			final float tmp = paint.measureText(list[i]);

			if (tmp > txtWidth) {
				txtWidth = tmp;
				n = i;
			}
		}

		float newSize = 0;
		for (int i = 0; i < 2; i++) {
			newSize = paint.getTextSize() * (width / paint.measureText(list[n]));
			paint.setTextSize(newSize);
		}
		return newSize;
	}
}
