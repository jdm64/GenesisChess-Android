package com.chess.genesis;

class AdapterItem
{
	public String name;
	public int id;

	public AdapterItem(final String Name, final int Id)
	{
		name = Name;
		id = Id;
	}

	public String toString()
	{
		return name;
	}
}
