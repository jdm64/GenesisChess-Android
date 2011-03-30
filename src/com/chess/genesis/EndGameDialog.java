package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

class EndGameDialog extends Dialog implements OnClickListener
{
	private static final String[] WON_CHECK = {"You Won", "Checkmate"};
	private static final String[] LOST_CHECK = {"You Lost", "Checkmate"};
	private static final String[] LOST_RESIGN = {"You Lost", "Resigned"};
	private static final String[] WON_RESIGN = {"You Won", "Resigned"};
	private static final String[] TIED_IMP = {"Game Tied", "Imposibility of Checkmate"};
	private static final String[] TIED_STALE = {"Game Tied", "Stalemate"};

	private static final Map<Integer,String[]> STATUS_MAP = createMap();
	
	private static Map<Integer, String[]> createMap()
	{
		final Map<Integer, String[]> map = new HashMap<Integer, String[]>();
		map.put(Piece.WHITE * Enums.WHITEMATE, WON_CHECK);
		map.put(Piece.WHITE * Enums.BLACKMATE, LOST_CHECK);
		map.put(Piece.WHITE * Enums.WHITERESIGN, LOST_RESIGN);
		map.put(Piece.WHITE * Enums.BLACKRESIGN, WON_RESIGN);
		map.put(Piece.WHITE * Enums.IMPOSSIBLE, TIED_IMP);
		map.put(Piece.WHITE * Enums.STALEMATE, TIED_STALE);

		map.put(Piece.BLACK * Enums.WHITEMATE, LOST_CHECK);
		map.put(Piece.BLACK * Enums.BLACKMATE, WON_CHECK);
		map.put(Piece.BLACK * Enums.WHITERESIGN, WON_RESIGN);
		map.put(Piece.BLACK * Enums.BLACKRESIGN, LOST_RESIGN);
		map.put(Piece.BLACK * Enums.IMPOSSIBLE, TIED_IMP);
		map.put(Piece.BLACK * Enums.STALEMATE, TIED_STALE);
		return map;
	}

	private final String title;
	private final String result;
	private final String psr_type;
	private final int diff;
	
	private String opponent;
	private String psr_score;

	public EndGameDialog(final Context context, final JSONObject json)
	{
		super(context);

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
		} catch (JSONException e) {
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
		setTitle(title);

		setContentView(R.layout.endgame);

		final Button close = (Button) findViewById(R.id.close);
		close.setOnClickListener(this);

		TextView msg = (TextView) findViewById(R.id.opponent);
		msg.setText(opponent);

		msg = (TextView) findViewById(R.id.result);
		msg.setText(result);

		msg = (TextView) findViewById(R.id.psr_type);
		msg.setText(psr_type);

		msg = (TextView) findViewById(R.id.psr_score);
		msg.setText(psr_score);

		if (diff > 0)
			msg.setTextColor(0xFF00FF00);
		else if (diff < 0)
			msg.setTextColor(0xFFFF0000);
	}

	public void onClick(final View v)
	{
		dismiss();
	}
}
