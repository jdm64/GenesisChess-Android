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

package com.chess.genesis.view;

import android.content.*;
import android.support.v4.widget.*;
import android.util.*;

public class DrawerLayout2 extends DrawerLayout
{
	public DrawerLayout2(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public DrawerLayout2(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public DrawerLayout2(Context context)
	{
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		int sizeSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

		super.onMeasure(sizeSpec, sizeSpec);
	}

	public void toggle(int gravity)
	{
		if (isDrawerOpen(gravity))
			closeDrawer(gravity);
		else
			openDrawer(gravity);
	}
}
