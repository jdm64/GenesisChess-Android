package com.chess.genesis;

class Enums
{
	// Play Types
	public final static int LOCAL_GAME = 1;
	public final static int ONLINE_GAME = 2;

	// Game Types
	public final static int REGULAR_CHESS = 1;
	public final static int GENESIS_CHESS = 2;

	// Opponent Types
	public final static int HUMAN_OPPONENT = 1;
	public final static int COMPUTER_OPPONENT = 2;

	// Opponent Selection
	public final static int RANDOM = 1;
	public final static int INVITE = 2;

	public static int GameType(String gametype)
	{
		if (gametype.equals("genesis"))
			return GENESIS_CHESS;
		if (gametype.equals("regular"))
			return REGULAR_CHESS;
		else
			return GENESIS_CHESS;
	}
}