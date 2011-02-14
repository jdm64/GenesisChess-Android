package com.chess.genesis;

import java.util.Arrays;

class IntArray
{
	int[] list = new int[0];

	// !SICK!
	// This only exists because Arrays.copyOf was added in API Level 9
	private int[] copyOf(int[] arr, int size)
	{
		int[] temp = new int[size];

		for (int i = 0; i < Math.min(arr.length, size); i++)
			temp[i] = arr[i];
		return temp;
	}

	public void clear()
	{
		list = new int[0];
	}

	public int size()
	{
		return list.length;
	}

	public int get(int index)
	{
		return list[index];
	}

	public void set(int index, int value)
	{
		list[index] = value;
	}

	public void push(int value)
	{
		list = copyOf(list, list.length + 1);
		list[list.length - 1] = value;
	}

	public int pop()
	{
		int end = list[list.length - 1];
		list = copyOf(list, list.length - 1);
		return end;
	}
}
