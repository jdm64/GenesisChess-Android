package com.chess.genesis;

class RegMoveList
{
	public RegMoveNode[] list;
	public int size;

	public RegMoveList()
	{
		size = 0;
		list = new RegMoveNode[320];
	}
}
