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

	// Game Status
	public final static int ACTIVE = 1;
	public final static int WHITEMATE = 2;
	public final static int BLACKMATE = 3;
	public final static int STALEMATE = 4;
	public final static int IMPOSSIBLE = 5;
	public final static int WHITERESIGN = 6;
	public final static int BLACKRESIGN = 7;

	public static int GameType(String gametype)
	{
		if (gametype.equals("genesis"))
			return GENESIS_CHESS;
		if (gametype.equals("regular"))
			return REGULAR_CHESS;
		throw new RuntimeException("unknown gametype: " + gametype);
	}

	public static String GameType(int gametype)
	{
		switch (gametype) {
		case GENESIS_CHESS:
			return "genesis";
		case REGULAR_CHESS:
			return "regular";
		}
		throw new RuntimeException("unknown gametype: " + String.valueOf(gametype));
	}

	public static int GameStatus(String gamestatus)
	{
		if (gamestatus.equals("active"))
			return ACTIVE;
		if (gamestatus.equals("whitemate"))
			return WHITEMATE;
		if (gamestatus.equals("blackmate"))
			return BLACKMATE;
		if (gamestatus.equals("stalemate"))
			return STALEMATE;
		if (gamestatus.equals("impossible"))
			return IMPOSSIBLE;
		if (gamestatus.equals("whiteresign"))
			return WHITERESIGN;
		if (gamestatus.equals("blackresign"))
			return BLACKRESIGN;
		throw new RuntimeException("unknown gamestatus: " + gamestatus);
	}
}
