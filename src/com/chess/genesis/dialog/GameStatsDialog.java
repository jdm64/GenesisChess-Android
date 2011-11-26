package com.chess.genesis;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

class GameStatsDialog extends BaseDialog implements OnClickListener
{
	private static final String[] WON_CHECK = {"You Won", "Checkmate"};
	private static final String[] LOST_CHECK = {"You Lost", "Checkmate"};
	private static final String[] LOST_RESIGN = {"You Lost", "Resigned"};
	private static final String[] WON_RESIGN = {"You Won", "Resigned"};
	private static final String[] WON_IDLE = {"You Won", "Idle"};
	private static final String[] LOST_IDLE = {"You Lost", "Idle"};
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
		map.put(Piece.WHITE * Enums.WHITEIDLE, LOST_IDLE);
		map.put(Piece.WHITE * Enums.BLACKIDLE, WON_IDLE);

		map.put(Piece.BLACK * Enums.WHITEMATE, LOST_CHECK);
		map.put(Piece.BLACK * Enums.BLACKMATE, WON_CHECK);
		map.put(Piece.BLACK * Enums.WHITERESIGN, WON_RESIGN);
		map.put(Piece.BLACK * Enums.BLACKRESIGN, LOST_RESIGN);
		map.put(Piece.BLACK * Enums.IMPOSSIBLE, TIED_IMP);
		map.put(Piece.BLACK * Enums.STALEMATE, TIED_STALE);
		map.put(Piece.BLACK * Enums.WHITEIDLE, WON_IDLE);
		map.put(Piece.BLACK * Enums.BLACKIDLE, LOST_IDLE);
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

	public void onClick(final View v)
	{
		dismiss();
	}
}
