package com.chess.genesis;

import java.util.Arrays;

public class IntArray
{
	int[] list = new int[0];

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
		list = Arrays.copyOf(list, list.length + 1);
		list[list.length - 1] = value;
	}

	public int pop()
	{
		int end = list[list.length - 1];
		list = Arrays.copyOf(list, list.length - 1);
		return end;
	}
}
