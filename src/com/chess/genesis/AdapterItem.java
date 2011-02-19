package com.chess.genesis;

class AdapterItem
{
	public String name;
	public int id;

	public AdapterItem(String Name, int Id)
	{
		name = Name;
		id = Id;
	}

	public String toString()
	{
		return name;
	}
}
