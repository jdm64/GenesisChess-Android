package com.chess.genesis.controller;

import android.content.*;
import com.chess.genesis.api.*;
import com.chess.genesis.engine.*;

public class ArchivedPlayer implements IPlayer
{
	private final String playerName;

	public ArchivedPlayer(int color, IGameModel model)
	{
		var data = model.getGameEntity();
		playerName = color == Piece.WHITE ? data.white : data.black;
	}

	@Override
	public boolean canClick(int stm)
	{
		return false;
	}

	@Override
	public String getStmName(boolean overwrite)
	{
		return playerName;
	}

	@Override
	public void finalizeMove(Move move, Context context)
	{
	}

	@Override
	public void takeTurn(Context context)
	{
	}
}
