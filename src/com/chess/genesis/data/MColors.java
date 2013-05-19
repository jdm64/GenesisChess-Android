/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.data;

public final class MColors
{
	private MColors()
	{
	}

	/*
	Android Style Colors
	R=0..3..6..9A.C..F
	G=0..34.6.89.BC...
	B=0..34.......C.E.
	*/

	public final static int WHITE = 0xffffffff;
	public final static int BLACK = 0xff000000;
	public final static int CLEAR = 0x00ffffff;
	public final static int GREY  = 0xff888888;
	public final static int GREY_LIGHT = 0xffcccccc;

	public final static int RED = 0xffee0000;
	public final static int RED_LIGHT = 0xffee4433;
	public final static int RED_DARK = 0xffcc0000;

	public final static int ORANGE = 0xffff8800;

	public final static int GREEN_TEAL = 0xff33bb44;
	public final static int GREEN_DARK = 0xff449900;

	public final static int TEAL_PASTEL = 0xffeeffff;

	public final static int BLUE_NEON_TR = 0x8800bbee;
	public final static int BLUE_NEON = 0xff00bbee;
	public final static int BLUE_DARK = 0xff3366aa;
	public final static int BLUE_NAVY = 0xff6688bb;
	public final static int BLUE_NAVY_DARK = 0xff446699;
	public final static int BLUE_PASTEL = 0xffcceeff;

	public final static int PURPLE_LIGHT = 0xffaa99cc;
	public final static int PURPLE_PASTEL = 0xffddddff;
}
