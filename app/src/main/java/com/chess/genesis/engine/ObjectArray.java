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

package com.chess.genesis.engine;

import java.util.function.*;
import androidx.annotation.*;

public class ObjectArray<Type>
{
	private final Supplier<Type> generator;
	protected Type[] list;

	public ObjectArray(Supplier<Type> instance)
	{
		this(instance, makeArray(0));
	}

	public ObjectArray(Supplier<Type> instance, Type[] arr)
	{
		generator = instance;
		list = arr;
	}

	@SuppressWarnings({"unchecked", "static-method"})
	private static <Type> Type[] makeArray(int size)
	{
		return (Type[]) new Object[size];
	}

	private void copy(int size)
	{
		Type[] temp = makeArray(size);
		System.arraycopy(list, 0, temp, 0, Math.min(list.length, size));
		list = temp;
	}

	public void clear()
	{
		list = makeArray(0);
	}

	public int size()
	{
		return list.length;
	}

	public void resize(int size)
	{
		copy(size);
	}

	public Type get(int index)
	{
		if (index >= list.length)
			copy(index + 1);
		if (list[index] == null)
			list[index] = generator.get();
		return list[index];
	}

	public void set(int index, Type value)
	{
		if (index >= list.length)
			copy(index + 1);
		list[index] = value;
	}

	public void push(Type value)
	{
		set(list.length, value);
	}

	public Type pop()
	{
		var end = top();
		copy(list.length - 1);
		return end;
	}

	public Type top()
	{
		return list[list.length - 1];
	}

	@NonNull
	@Override
	public String toString()
	{
		return arrayToString(list, " ");
	}

	public static String arrayToString(Object[] array, String delim)
	{
		var str = new StringBuilder();

		for (var element : array)
			str.append(element).append(delim);
		return str.toString();
	}
}
