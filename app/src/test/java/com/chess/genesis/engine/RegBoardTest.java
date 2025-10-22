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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

public class RegBoardTest extends AbstractBoardTest
{
	@Override
	BaseBoard getBoard()
	{
		return new RegBoard();
	}

	static Object[][] testParseFen_Params()
	{
		return new Object[][] {
		    {"rnbqkbnrpppppppp32PPPPPPPPRNBQKBNR:KQkq::0", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"},
		    {"rnbqkbnrp1ppppp8p1pP21PP1PPPPPRNBQKBNR:KQkq:b6:6", "rnbqkbnr/p1ppppp1/7p/1pP5/8/8/PP1PPPPP/RNBQKBNR w KQkq b6 0 4"},
		    {"2r3k1p4pp5p2n1p5P2qr1Q4P1R3P4PP3R3K:::46", "2r3k1/p4pp1/4p2n/1p5P/2qr1Q2/2P1R3/P4PP1/2R3K1 w - - 0 24"},
		    {"rnbqkb2pppppp1P32PPPPPP2RNBQKB:::22", "rnbqkb2/pppppp1P/8/8/8/8/PPPPPP2/RNBQKB2 w - - 0 12"},
		    {"r3k2r48R3K2R:KQkq::52", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 27"},
		    {"r1r2k7p1p2p2np2pPp5PnN1N6P2P5PP1R1R3K:::51", "r1r2k2/5p1p/2p2np1/1pPp4/1PnN1N2/4P2P/5PP1/R1R3K1 b - - 0 26"},
		    {"5b1r2p1k3p4pQp4q4r1n1R10PP1B2NP4R2K:::53", "5b1r/2p1k3/p4pQp/4q3/1r1n1R2/8/PP1B2NP/4R2K b - - 0 27"}
		};
	}

	static Object[][] testParseAndMove_Params()
	{
		return new Object[][] {
		    {"rnbqkbnrpppppppp32PPPPPPPPRNBQKBNR:KQkq::0", "e2e4 e7e5 f1c4 g7g6", "rnbqkbnrpppp1p1p6p5p5B1P11PPPP1PPPRNBQK1NR:KQkq::4"},
		    {"rnbqkbnrpppppppp32PPPPPPPPRNBQKBNR:KQkq::0", "c2c4 f7f5 c4c5 d7d5", "rnbqkbnrppp1p1pp10Pp1p18PP1PPPPPRNBQKBNR:KQkq:d6:4"},
		    {"r3k2r48R3K2R:KQkq::52", "O-O", "r3k2r48R4RK:kq::53"},
		    {"r3k2r48R3K2R:KQkq::52", "O-O-O", "r3k2r50KR3R:kq::53"},
		    {"r3k2r48R3K2R:KQkq::53", "O-O", "r4rk49R3K2R:KQ::54"},
		    {"r3k2r48R3K2R:KQkq::53", "O-O-O", "2kr3r48R3K2R:KQ::54"},
		    {"r3k2r48R3K2R:KQkq::53", "a8b8", "1r2k2r48R3K2R:KQk::54"},
		    {"r3k2r48R3K2R:KQkq::53", "e8d8", "r2k3r48R3K2R:KQ::54"},
		    {"rnbqkb2pppppp1P32PPPPPP2RNBQKB:::22", "h7h8R", "rnbqkb1Rpppppp34PPPPPP2RNBQKB:::23"},
		    {"2r3k1p4pp5p2n1p5P2qr1Q4P1R3P4PP3R3K:::46", "f4d4 c4d4 c3c4", "2r3k1p4pp5p2n1p5P2Pq8R3P4PP3R3K:::49"},
		    {"rnbqkbnrp1ppppp8p1pP21PP1PPPPPRNBQKBNR:KQkq:b6:6", "c5b6", "rnbqkbnrp1ppppp2P5p24PP1PPPPPRNBQKBNR:KQkq::7"}
		};
	}

	static Object[][] testGenMoves_Params()
	{
		return new Object[][] {
		    {"35K:::1", "d4", "c3 c4 c5 d3 d5 e3 e4 e5", ""},
		    {"35Q:::1", "d4", "a1 a4 a7 b2 b4 b6 c3 c4 c5 d1 d2 d3 d5 d6 d7 d8 e3 e4 e5 f2 f4 f6 g1 g4 g7 h4 h8", ""},
		    {"35R:::1", "d4", "a4 b4 c4 d1 d2 d3 d5 d6 d7 d8 e4 f4 g4 h4", ""},
		    {"35B:::1", "d4", "a1 a7 b2 b6 c3 c5 e3 e5 f2 f6 g1 g7 h8", ""},
		    {"35N:::1", "d4", "b3 b5 c2 c6 e2 e6 f3 f5", ""},
		    {"35P:::1", "d4", "d5", ""},

		    {"17p3p5p5p1Kp5p1p6p:::1", "d4", "c4 c5 d3 e5", "c3 d5 e3 e4"},
		    {"17p3p5p5p1Qp5p1p6p:::1", "d4", "c4 c5 d3 e5", "b4 b6 c3 d2 d5 e3 e4 f6"},
		    {"17p3p5p5p1Rp5p1p6p:::1", "d4", "c4 d3", "b4 d2 d5 e4"},
		    {"17p3p5p5p1Bp5p1p6p:::1", "d4", "c5 e5", "b6 c3 e3 f6"},
		    {"17p3p5p5p1Pp5p1p6p:::1", "d4", "", ""},

		    {"19p6p1p5pK1p5p5p3p:::9", "d4", "c3 d5 e3 e4", "c4 c5 d3 e5"},
		    {"19p6p1p5pQ1p5p5p3p:::9", "d4", "c3 d5 e3 e4", "b2 c4 c5 d3 d6 e5 f2 f4"},
		    {"19p6p1p5pR1p5p5p3p:::9", "d4", "d5 e4", "c4 d3 d6 f4"},
		    {"19p6p1p5pB1p5p5p3p:::9", "d4", "c3 e3", "b2 c5 e5 f2"},
		    {"19p6p1p5pP1p5p5p3p:::9", "d4", "d5", "c5 e5"},

		    {"18p10p5N5p10p:::1", "d4", "b5 c2 e6 f3", "b3 c6 e2 f5"},
		    {"20p4p9N9p4p:::1", "d4", "b3 c6 e2 f5", "b5 c2 e6 f3"},
		};
	}

	static Object[][] testGetters_Params()
	{
		return new Object[][] {
		    {"1r4k7p7p3p2pBnQ1Pp7P2qP6B1R5K:::48", "0 0 0 0 0 0 0 0 0 0 0 0 0", "0 0 1 2 1 3 0 5 2 0 1 0 0"},
		    {"rnbqkbnrpppppppp32PPPPPPPPRNBQKBNR:KQkq::0", "0 0 0 0 0 0 0 0 0 0 0 0 0", "0 0 0 0 0 0 0 0 0 0 0 0 0"},
		    {"r3k2r48R3K2R:KQkq::53", "0 0 0 0 0 0 0 0 0 0 0 0 0", "0 1 0 2 2 8 0 8 2 2 0 1 0"}
		};
	}

	static Object[][] testIsMate_Params()
	{
		return new Object[][] {
		    {"5b1r2p1k3p4pQp4q4r1n1R10PP1B2NP4R2K:::53", Board.NOT_MATE},
		    {"1r2k2rpp1b2p3p4p4P4B4pPP11R4n2K1q:::74", Board.CHECK_MATE},
		    {"K6k1rr:::88", Board.STALE_MATE}
		};
	}

	static Object[][] testBadMoves_Params()
	{
		return new Object[][]{
		    {"rnbqkbnrpppppppp32PPPPPPPPRNBQKBNR:KQkq::0", "Ka1", Board.INVALID_FORMAT},
		};
	}
}
