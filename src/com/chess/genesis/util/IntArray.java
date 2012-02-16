package com.chess.genesis;

class IntArray
{
	private int[] list = new int[0];

	// !SICK!
	// This only exists because Arrays.copyOf was added in API Level 9
	public static int[] copyOf(final int[] arr, final int size)
	{
		final int[] temp = new int[size];

		for (int i = 0; i < Math.min(arr.length, size); i++)
			temp[i] = arr[i];
		return temp;
	}

	public static int[] clone(final int[] arr)
	{
		final int[] temp = new int[arr.length];

		for (int i = 0; i < arr.length; i++)
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

	public void resize(final int size)
	{
		list = copyOf(list, size);
	}

	public int get(final int index)
	{
		if (index >= list.length)
			list = copyOf(list, index + 1);
		return list[index];
	}

	public void set(final int index, final int value)
	{
		if (index >= list.length)
			list = copyOf(list, index + 1);
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

	public int top()
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
