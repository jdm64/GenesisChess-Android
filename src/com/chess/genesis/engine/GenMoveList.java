package com.chess.genesis;

class GenMoveList
{
	public GenMoveNode[] list;
	public int size;

	public GenMoveList()
	{
		size = 0;
		list = new GenMoveNode[320];
	}
}
