package com.chess.genesis;

import java.util.Arrays;

class ObjectArray<Type>
{
	private Type[] list = (Type[]) new Object[0];

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
		list = Arrays.copyOf(list, size);
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
		list = Arrays.copyOf(list, list.length + 1);
		list[list.length - 1] = value;
	}

	public Type pop()
	{
		Type end = list[list.length - 1];
		list = Arrays.copyOf(list, list.length - 1);
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
