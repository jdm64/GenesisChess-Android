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

package com.chess.genesis.data;

import android.content.*;
import android.content.SharedPreferences.*;

public class PrefEdit extends Pref
{
	private final Editor editor;

	public PrefEdit(final Context context)
	{
		super(context);
		editor = edit();
	}

	public PrefEdit putString(final int _key, final String _value)
	{
		editor.putString(key(_key), _value);
		return this;
	}

	public PrefEdit putString(final int _key)
	{
		final KeyDef kd = getStringKeyDef(_key);
		editor.putString(kd.key, (String) kd.def);
		return this;
	}

	public PrefEdit putLong(final int _key, final long _value)
	{
		editor.putLong(key(_key), _value);
		return this;
	}

	public PrefEdit putLong(final int _key)
	{
		final KeyDef kd = getLongKeyDef(_key);
		editor.putLong(kd.key, (Long) kd.def);
		return this;
	}

	public PrefEdit putInt(final int _key, final int _value)
	{
		editor.putInt(key(_key), _value);
		return this;
	}

	public PrefEdit putInt(final int _key)
	{
		final KeyDef kd = getIntKeyDef(_key);
		editor.putInt(kd.key, (Integer) kd.def);
		return this;
	}

	public PrefEdit putBool(final int _key, final boolean _value)
	{
		editor.putBoolean(key(_key), _value);
		return this;
	}

	public PrefEdit putBool(final int _key)
	{
		final KeyDef kd = getBoolKeyDef(_key);
		editor.putBoolean(kd.key, (Boolean) kd.def);
		return this;
	}

	public PrefEdit commit()
	{
		editor.commit();
		return this;
	}
}
