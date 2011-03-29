package com.chess.genesis;

class IntArray
{
	private int[] list = new int[0];

	// !SICK!
	// This only exists because Arrays.copyOf was added in API Level 9
	private int[] copyOf(final int[] arr, final int size)
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

	public int get(final int index)
	{
		return list[index];
	}

	public void set(final int index, final int value)
	{
		list[index] = value;
	}

	public void push(final int value)
	{
		list = copyOf(list, list.length + 1);
		list[list.length - 1] = value;
	}

	public int pop()
	{
		final int end = list[list.length - 1];
		list = copyOf(list, list.length - 1);
		return end;
	}
}
