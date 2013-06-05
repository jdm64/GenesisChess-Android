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

public class IntArray
{
	private IntArray()
	{
	}

	public static int[] clone(final int[] arr, final int size)
	{
		final int[] temp = new int[size];
		System.arraycopy(arr, 0, temp, 0, Math.min(arr.length, size));
		return temp;
	}

	public static int[] clone(final int[] arr)
	{
		return clone(arr, arr.length);
	}
}
