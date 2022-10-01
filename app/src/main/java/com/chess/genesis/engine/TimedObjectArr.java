/* GenesisChess, an Android chess application
 * Copyright 2022, Justin Madru (justin.jdm64@gmail.com)
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
package com.chess.genesis.engine;

import java.util.function.*;
import androidx.annotation.*;

public class TimedObjectArr<Type> extends ObjectArray<Type>
{
	private long[] times;

	public TimedObjectArr(Supplier<Type> instance)
	{
		super(instance);
		times = makeArray(0);
	}

	private static long[] makeArray(int size)
	{
		return new long[size];
	}

	private void copy(int size)
	{
		var temp = makeArray(size);
		System.arraycopy(times, 0, temp, 0, Math.min(times.length, size));
		times = temp;
	}

	@Override
	public void clear()
	{
		times = makeArray(0);
		super.clear();
	}

	@Override
	public void resize(int size)
	{
		copy(size);
		super.resize(size);
	}

	@Override
	public Type get(int index)
	{
		if (index >= times.length)
			copy(index + 1);
		if (times[index] == 0)
			times[index] = System.currentTimeMillis();
		return super.get(index);
	}

	public void setWithTime(int index, Type value, long time)
	{
		if (index >= times.length)
			copy(index + 1);
		times[index] = time;
		super.set(index, value);
	}

	@Override
	public void set(int index, Type value)
	{
		setWithTime(index, value, System.currentTimeMillis());
	}

	public void pushWithTime(Type value, long time)
	{
		setWithTime(times.length, value, time);
	}

	@Override
	public void push(Type value)
	{
		pushWithTime(value, System.currentTimeMillis());
	}

	@Override
	public Type pop()
	{
		var end = times[times.length - 1];
		copy(times.length - 1);
		return super.pop();
	}

	@NonNull
	@Override
	public String toString()
	{
		var str = new StringBuilder();
		for (int i = 0; i < list.length; i++)
			str.append(list[i]).append(",").append(times[i]).append(" ");
		return str.toString();
	}
}
