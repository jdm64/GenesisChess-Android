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
import android.content.SharedPreferences.Editor;
import android.content.res.*;
import android.preference.*;

public class Pref
{
	private final static int KEY_INDX = 0;
	private final static int DEF_INDX = 1;

	private final SharedPreferences pref;
	private final Resources res;

	public static abstract class KeyDef
	{
		public String key;
		public Object def;

		public void set(final TypedArray arr)
		{
			key = arr.getString(KEY_INDX);
			def = getDef(arr);
		}

		public abstract Object getDef(TypedArray arr);
	}

	public Pref(final Context context)
	{
		res = context.getResources();
		pref = getPref(context);
	}

	private static SharedPreferences getPref(final Context cntx)
	{
		return PreferenceManager.getDefaultSharedPreferences(cntx);
	}

	Editor edit()
	{
		return pref.edit();
	}

	public void setChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
	{
		pref.registerOnSharedPreferenceChangeListener(listener);
	}

	private static KeyDef setKeyDef(final Resources _res, final int _key, final KeyDef _kd)
	{
		final TypedArray arr = _res.obtainTypedArray(_key);
		_kd.set(arr);
		arr.recycle();
		return _kd;
	}

	private static String key(final Resources _res, final int _key)
	{
		return setKeyDef(_res, _key, new KeyDef() {
			@Override
			public Object getDef(final TypedArray arr) {
				return null;
			}
		}).key;
	}

	public static String key(final Context _cntx, final int _key)
	{
		return key(_cntx.getResources(), _key);
	}

	public String key(final int _key)
	{
		return key(res, _key);
	}

	private static KeyDef getStringKeyDef(final Resources _res, final int _key)
	{
		return setKeyDef(_res, _key, new KeyDef() {
			@Override
			public Object getDef(final TypedArray arr) {
				return arr.getString(DEF_INDX);
			}
		});
	}

	KeyDef getStringKeyDef(final int _key)
	{
		return getStringKeyDef(res, _key);
	}

	private static KeyDef getLongKeyDef(final Resources _res, final int _key)
	{
		return setKeyDef(_res, _key, new KeyDef() {
			@Override
			public Object getDef(final TypedArray arr) {
				// !SICK! TypedArray doesn't have long
				return Long.parseLong(arr.getString(DEF_INDX));
			}
		});
	}

	KeyDef getLongKeyDef(final int _key)
	{
		return getLongKeyDef(res, _key);
	}

	private static KeyDef getIntKeyDef(final Resources _res, final int _key)
	{
		return setKeyDef(_res, _key, new KeyDef() {
			@Override
			public Object getDef(final TypedArray arr) {
				return arr.getInt(DEF_INDX, 0);
			}
		});
	}

	KeyDef getIntKeyDef(final int _key)
	{
		return getIntKeyDef(res, _key);
	}

	private static KeyDef getBoolKeyDef(final Resources _res, final int _key)
	{
		return setKeyDef(_res, _key, new KeyDef() {
			@Override
			public Object getDef(final TypedArray arr) {
				return arr.getBoolean(DEF_INDX, false);
			}
		});
	}

	KeyDef getBoolKeyDef(final int _key)
	{
		return getBoolKeyDef(res, _key);
	}

	private static String getString(final SharedPreferences _pref, final Resources _res, final int _key)
	{
		final KeyDef kd = getStringKeyDef(_res, _key);
		return _pref.getString(kd.key, (String) kd.def);
	}

	public static String getString(final Context _cntx, final int _key)
	{
		return getString(getPref(_cntx), _cntx.getResources(), _key);
	}

	public String getString(final int _key)
	{
		return getString(pref, res, _key);
	}

	private static long getLong(final SharedPreferences _pref, final Resources _res, final int _key)
	{
		final KeyDef kd = getLongKeyDef(_res, _key);
		return _pref.getLong(kd.key, (Long) kd.def);
	}

	public static long getLong(final Context _cntx, final int _key)
	{
		return getLong(getPref(_cntx), _cntx.getResources(), _key);
	}

	public long getLong(final int _key)
	{
		return getLong(pref, res, _key);
	}

	private static int getInt(final SharedPreferences _pref, final Resources _res, final int _key)
	{
		final KeyDef kd = getIntKeyDef(_res, _key);
		return _pref.getInt(kd.key, (Integer) kd.def);
	}

	public static int getInt(final Context _cntx, final int _key)
	{
		return getInt(getPref(_cntx), _cntx.getResources(), _key);
	}

	public int getInt(final int _key)
	{
		return getInt(pref, res, _key);
	}

	private static boolean getBool(final SharedPreferences _pref, final Resources _res, final int _key)
	{
		final KeyDef kd = getBoolKeyDef(_res, _key);
		return _pref.getBoolean(kd.key, (Boolean) kd.def);
	}

	public static boolean getBool(final Context _cntx, final int _key)
	{
		return getBool(getPref(_cntx), _cntx.getResources(), _key);
	}

	public boolean getBool(final int _key)
	{
		return getBool(pref, res, _key);
	}
}
