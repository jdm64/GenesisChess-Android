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

package com.chess.genesis.dialog;

import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.engine.*;
import org.json.*;

public class GameStatsDialog extends BaseDialog implements OnClickListener
{
	private static final String[] WON_CHECK = {"You Won", "Checkmate"};
	private static final String[] LOST_CHECK = {"You Lost", "Checkmate"};
	private static final String[] LOST_RESIGN = {"You Lost", "Resigned"};
	private static final String[] WON_RESIGN = {"You Won", "Resigned"};
	private static final String[] WON_IDLE = {"You Won", "Idle"};
	private static final String[] LOST_IDLE = {"You Lost", "Idle"};
	private static final String[] TIED_IMP = {"Game Tied", "Imposibility of Checkmate"};
	private static final String[] TIED_STALE = {"Game Tied", "Stalemate"};
	private static final String[] DRAW_GAME = {"Game Tied", "Draw"};

	private static final SparseArray<String[]> STATUS_MAP = createMap();

	private static SparseArray<String[]> createMap()
	{
		final SparseArray<String[]> map = new SparseArray<String[]>();
		map.put(Piece.WHITE * Enums.WHITEMATE, WON_CHECK);
		map.put(Piece.WHITE * Enums.BLACKMATE, LOST_CHECK);
		map.put(Piece.WHITE * Enums.WHITERESIGN, LOST_RESIGN);
		map.put(Piece.WHITE * Enums.BLACKRESIGN, WON_RESIGN);
		map.put(Piece.WHITE * Enums.IMPOSSIBLE, TIED_IMP);
		map.put(Piece.WHITE * Enums.STALEMATE, TIED_STALE);
		map.put(Piece.WHITE * Enums.WHITEIDLE, LOST_IDLE);
		map.put(Piece.WHITE * Enums.BLACKIDLE, WON_IDLE);
		map.put(Piece.WHITE * Enums.DRAW, DRAW_GAME);

		map.put(Piece.BLACK * Enums.WHITEMATE, LOST_CHECK);
		map.put(Piece.BLACK * Enums.BLACKMATE, WON_CHECK);
		map.put(Piece.BLACK * Enums.WHITERESIGN, WON_RESIGN);
		map.put(Piece.BLACK * Enums.BLACKRESIGN, LOST_RESIGN);
		map.put(Piece.BLACK * Enums.IMPOSSIBLE, TIED_IMP);
		map.put(Piece.BLACK * Enums.STALEMATE, TIED_STALE);
		map.put(Piece.BLACK * Enums.WHITEIDLE, WON_IDLE);
		map.put(Piece.BLACK * Enums.BLACKIDLE, LOST_IDLE);
		map.put(Piece.BLACK * Enums.DRAW, DRAW_GAME);
		return map;
	}

	private final String title;
	private final String result;
	private final String psr_type;
	private final int diff;

	private final String opponent;
	private final String psr_score;

	public GameStatsDialog(final Context context, final Bundle bundle)
	{
		super(context, BaseDialog.CANCEL);

		final int from, to;
		final int status = Integer.valueOf(bundle.getString("status"));
		final int eventtype = Integer.valueOf(bundle.getString("eventtype"));
		final int ycol = bundle.getInt("yourcolor");

		final String[] statusArr = STATUS_MAP.get(status * ycol);
		String gametype = Enums.GameType(Integer.valueOf(bundle.getString("gametype")));
		gametype = gametype.substring(0,1).toUpperCase() + gametype.substring(1);

		if (ycol == Piece.WHITE) {
			opponent = bundle.getString("black");
			from = Integer.valueOf(bundle.getString("w_psrfrom"));
			to = Integer.valueOf(bundle.getString("w_psrto"));
		} else {
			opponent = bundle.getString("white");
			from = Integer.valueOf(bundle.getString("b_psrfrom"));
			to = Integer.valueOf(bundle.getString("b_psrto"));
		}

		diff = to - from;
		final String sign = (diff >= 0)? "+" : "-";

		title = statusArr[0];
		result = statusArr[1];
		psr_type = gametype + " PSR :";

		if (eventtype == Enums.INVITE)
			psr_score = "None (Invite Game)";
		else
			psr_score = sign + String.valueOf(Math.abs(diff)) + " (" + String.valueOf(to) + ")";
	}

	public GameStatsDialog(final Context context, final JSONObject json)
	{
		super(context, BaseDialog.CANCEL);

		String[] statusArr = null;
		String gametype = null, gameid = null, sign = null;
		int eventtype = 0, ycol = 0, w_from = 0, w_to = 0, b_from = 0, b_to = 0;

		try {
			gameid = json.getString("gameid");
			ycol = json.getInt("yourcolor");
			statusArr = STATUS_MAP.get(json.getInt("status") * ycol);
			gametype = json.getString("gametype");
			gametype = gametype.substring(0,1).toUpperCase() + gametype.substring(1);
			eventtype = Integer.valueOf(json.getString("eventtype"));

			if (ycol == Piece.WHITE)
				opponent = json.getString("black_name");
			else
				opponent = json.getString("white_name");

			if (eventtype != Enums.INVITE) {
				w_from = json.getJSONObject("white").getInt("from");
				w_to = json.getJSONObject("white").getInt("to");

				b_from = json.getJSONObject("black").getInt("from");
				b_to = json.getJSONObject("black").getInt("to");
			}
		} catch (final JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		final int to = (ycol == Piece.WHITE)? w_to : b_to;
		diff = (ycol == Piece.WHITE)? (w_to - w_from) : (b_to - b_from);
		sign = (diff >= 0)? "+" : "-";

		title = statusArr[0];
		result = statusArr[1];
		psr_type = gametype + " PSR :";

		if (eventtype == Enums.INVITE)
			psr_score = "None (Invite Game)";
		else
			psr_score = sign + String.valueOf(Math.abs(diff)) + " (" + String.valueOf(to) + ")";

		final GameDataDB db = new GameDataDB(context);
		db.archiveNetworkGame(gameid, w_from, w_to, b_from, b_to);
		db.close();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(title);
		setBodyView(R.layout.dialog_endgame);
		setButtonTxt(R.id.cancel, "Close");

		// Set TextViews
		final int list[] = new int[]{R.id.opponent, R.id.result,
			R.id.psr_type, R.id.psr_score};
		final String data[] = new String[]{opponent, result,
			psr_type, psr_score};

		TextView msg = null;
		for (int i = 0; i < list.length; i++) {
			msg = (TextView) findViewById(list[i]);
			msg.setText(data[i]);
		}

		// psr_score must be last in list[]
		if (diff > 0)
			msg.setTextColor(0xff4e9a06);
		else if (diff < 0)
			msg.setTextColor(0xffcc0000);
	}

	@Override
	public void onClick(final View v)
	{
		dismiss();
	}
}
