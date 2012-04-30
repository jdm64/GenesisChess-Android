/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.util;

public class BoolArray
{
	private boolean[] list = new boolean[0];

	// !SICK!
	// This only exists because Arrays.copyOf was added in API Level 9
	private static boolean[] copyOf(final boolean[] arr, final int size)
	{
		final boolean[] temp = new boolean[size];

		for (int i = 0; i < Math.min(arr.length, size); i++)
			temp[i] = arr[i];
		return temp;
	}

	public void clear()
	{
		list = new boolean[0];
	}

	public int size()
	{
		return list.length;
	}

	public void resize(final int size)
	{
		list = copyOf(list, size);
	}

	public boolean get(final int index)
	{
		if (index >= list.length)
			list = copyOf(list, index + 1);
		return list[index];
	}

	public void set(final int index, final boolean value)
	{
		if (index >= list.length)
			list = copyOf(list, index + 1);
		list[index] = value;
	}

	public void push(final boolean value)
	{
		list = copyOf(list, list.length + 1);
		list[list.length - 1] = value;
	}

	public boolean pop()
	{
		final boolean end = list[list.length - 1];
		list = copyOf(list, list.length - 1);
		return end;
	}

	public boolean top()
	{
		return list[list.length - 1];
	}

	@Override
	public String toString()
	{
		final StringBuffer str = new StringBuffer();

		for (int i = 0; i < list.length; i++)
			str.append(String.valueOf(list[i]) + " ");
		return str.toString();
	}
}
