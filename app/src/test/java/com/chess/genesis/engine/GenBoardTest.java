/* GenesisChess, an Android chess application
 * Copyright 2022, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chess.genesis.engine;

class GenBoardTest extends AbstractBoardTest
{
	@Override
	BaseBoard getBoard()
	{
		return new GenBoard();
	}

	static Object[][] testParseFen_Params()
	{
		return new Object[][] {
		    {":ppppppppnnbbrrqkPPPPPPPPNNBBRRQK:0", "8/8/8/8/8/8/8/8 w ppppppppnnbbrrqkPPPPPPPPNNBBRRQK 0 1"},
		    {"11K9k:ppppppppnnbbrrqPPPPPPPPNNBBRRQ:2", "8/3K4/5k2/8/8/8/8/8 w ppppppppnnbbrrqPPPPPPPPNNBBRRQ 0 2"},
		    {"10P6r3Q4k18K:ppppppppnnbbrqPPPPPPPNNBBRR:6", "8/2P5/1r3Q2/2k5/8/5K2/8/8 w ppppppppnnbbrqPPPPPPPNNBBRR 0 4"},
		    {"1r4k7p7p3p2pBnQ1Pp7P2qP6B1R5K::48", "1r4k1/6p1/6p1/2p2pBn/Q1Pp4/3P2qP/6B1/R5K1 w - 0 25"}
		};
	}

	static Object[][] testParseAndMove_Params()
	{
		return new Object[][] {
		    {":ppppppppnnbbrrqkPPPPPPPPNNBBRRQK:0", "Ka1 Kh8 Ba8 Nh1 Pb4 Pg5 Qd6 Re3", "B6k11Q10p2P10r11K6n:pppppppnbbrqPPPPPPPNNBRR:8"},
		    {"11K9k:ppppppppnnbbrrqPPPPPPPPNNBBRRQ:2", "Qa8 Qb8", "Qq9K9k:ppppppppnnbbrrPPPPPPPPNNBBRR:4"},
		    {"10P6r3Q4k18K:ppppppppnnbbrqPPPPPPPNNBBRR:6", "f6b6 c5c4", "10P6Q16k10K:ppppppppnnbbrqPPPPPPPNNBBRR:8"}
		};
	}

	static Object[][] testGenMoves_Params()
	{
		return new Object[][] {
		    {"35K::1", "d4", "c3 c4 c5 d3 d5 e3 e4 e5", ""},
		    {"35Q::1", "d4", "a1 a4 a7 b2 b4 b6 c3 c4 c5 d1 d2 d3 d5 d6 d7 d8 e3 e4 e5 f2 f4 f6 g1 g4 g7 h4 h8", ""},
		    {"35R::1", "d4", "a4 b4 c4 d1 d2 d3 d5 d6 d7 d8 e4 f4 g4 h4", ""},
		    {"35B::1", "d4", "a1 a7 b2 b6 c3 c5 e3 e5 f2 f6 g1 g7 h8", ""},
		    {"35N::1", "d4", "b3 b5 c2 c6 e2 e6 f3 f5", ""},
		    {"35P::1", "d4", "c4 d3 d5 e4", ""},

		    {"17p3p5p5p1Kp5p1p6p::9", "d4", "c4 c5 d3 e5", "c3 d5 e3 e4"},
		    {"17p3p5p5p1Qp5p1p6p::9", "d4", "c4 c5 d3 e5", "b4 b6 c3 d2 d5 e3 e4 f6"},
		    {"17p3p5p5p1Rp5p1p6p::9", "d4", "c4 d3", "b4 d2 d5 e4"},
		    {"17p3p5p5p1Bp5p1p6p::9", "d4", "c5 e5", "b6 c3 e3 f6"},
		    {"17p3p5p5p1Pp5p1p6p::9", "d4", "c4 d3", "c3 e3"},

		    {"19p6p1p5pK1p5p5p3p::9", "d4", "c3 d5 e3 e4", "c4 c5 d3 e5"},
		    {"19p6p1p5pQ1p5p5p3p::9", "d4", "c3 d5 e3 e4", "b2 c4 c5 d3 d6 e5 f2 f4"},
		    {"19p6p1p5pR1p5p5p3p::9", "d4", "d5 e4", "c4 d3 d6 f4"},
		    {"19p6p1p5pB1p5p5p3p::9", "d4", "c3 e3", "b2 c5 e5 f2"},
		    {"19p6p1p5pP1p5p5p3p::9", "d4", "d5 e4", "c5 e5"},

		    {"18p10p5N5p10p::1", "d4", "b5 c2 e6 f3", "b3 c6 e2 f5"},
		    {"20p4p9N9p4p::1", "d4", "b3 c6 e2 f5", "b5 c2 e6 f3"},
		};
	}

	static Object[][] testGetters_Params()
	{
		return new Object[][] {
		    {"1r4k7p7p3p2pBnQ1Pp7P2qP6B1R5K::48", "0 0 0 0 0 0 0 0 0 0 0 0 0", "0 0 1 2 1 3 0 5 2 0 1 0 0"},
		    {"1r4k7p7p3p2pBnQ1Pp7P2qP6B1R5K:pppnbbrPPPPPNNR:48", "0 0 1 2 1 3 0 5 2 0 1 0 0", "0 0 0 0 0 0 0 0 0 0 0 0 0"}
		};
	}
}
