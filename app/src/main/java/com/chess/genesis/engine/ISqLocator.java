package com.chess.genesis.engine;

public interface ISqLocator
{
	ISquare getSq(int index);

	IBoardSq getBoardSq(int index);

	IPlaceSq getPlaceSq(int index);
}
