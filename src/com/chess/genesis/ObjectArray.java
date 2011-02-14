package com.chess.genesis;

import java.util.Arrays;

class ObjectArray<Type>
{
	private Type[] list = (Type[]) new Object[0];

	// !SICK!
	// This only exists because Arrays.copyOf was added in API Level 9
	private Type[] copyOf(Type[] arr, int size)
	{
		Type[] temp = (Type[]) new Object[size];

		for (int i = 0; i < Math.min(arr.length, size); i++)
			temp[i] = arr[i];
		return temp;
	}

	public void clear()
	{
		list = (Type[]) new Object[0];
	}

	public int size()
	{
		return list.length;
	}

	public void resize(int size)
	{
		list = copyOf(list, size);
	}

	public Type get(int index)
	{
		return list[index];
	}

	public void set(int index, Type value)
	{
		list[index] = value;
	}

	public void push(Type value)
	{
		list = copyOf(list, list.length + 1);
		list[list.length - 1] = value;
	}

	public Type pop()
	{
		Type end = list[list.length - 1];
		list = copyOf(list, list.length - 1);
		return end;
	}

	public Type top()
	{
		return list[list.length - 1];
	}

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < list.length; i++)
			str.append(list[i].toString() + " ");
		return str.toString();
	}
}
