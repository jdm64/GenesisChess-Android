package com.chess.genesis;

class MoveList
{
	public MoveNode[] list;
	public int size;

	public MoveList()
	{
		size = 0;
		list = new MoveNode[320];
	}
}
