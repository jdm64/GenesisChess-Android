/* GenesisChess, an Android chess application
 * Copyright 2018, Justin Madru (justin.jdm64@gmail.com)
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

import java.util.AbstractMap.*;
import java.util.Map.*;
import android.app.*;
import android.app.AlertDialog.*;
import android.view.*;
import androidx.fragment.app.DialogFragment;

class DialogUtil
{
	private DialogUtil() {}

	public static Entry<View, Builder> createViewBuilder(DialogFragment dialog, int resourceId)
	{
		return create(dialog.getActivity(), resourceId);
	}

	private static Entry<View, Builder> create(Activity activity, int resourceId)
	{
		Builder builder = new Builder(activity);
		View view = activity.getLayoutInflater().inflate(resourceId, null);
		builder.setView(view);
		return new SimpleEntry<>(view, builder);
	}
}
