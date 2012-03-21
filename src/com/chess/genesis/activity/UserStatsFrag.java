package com.chess.genesis;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class UserStatsFrag extends BaseContentFrag
{
	public final static String TAG = "USERSTATS";

	private final static int GEN_RAN = 0;
	private final static int GEN_INV = 1;
	private final static int REG_RAN = 2;
	private final static int REG_INV = 3;

	private final static int WIN = 0;
	private final static int LOS = 1;
	private final static int TIE = 2;
	private final static int RES = 3;

	private Bundle settings;
	private ProgressMsg progress;
	private NetworkClient net;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
		try {
			switch (msg.what) {
			case NetworkClient.USER_STATS:
				final JSONObject json = (JSONObject) msg.obj;
				if (json.getString("result").equals("error")) {
					progress.remove();
					Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return;
				}
				loadStats(json);
				progress.remove();
				break;
			case StatsLookupDialog.MSG:
				final String username = (String) msg.obj;

				progress.setText("Retrieving Stats");
				net.user_stats(username);
				(new Thread(net)).start();
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		}
	};

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		initBaseContentFrag();

		final View view = inflater.inflate(R.layout.fragment_userstats, container, false);

		settings = (savedInstanceState != null)? savedInstanceState : getArguments();

		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act);

		final RobotoText txt = (RobotoText) view.findViewById(R.id.username);
		txt.setText(settings.getString("username"));

		final int[] list = new int[]{R.id.apsr, R.id.total_games, R.id.total_wins,
			R.id.total_losses, R.id.total_resigns, R.id.total_ties};
		for (int id : list) {
			final TabText item = (TabText) view.findViewById(id);
			item.setOnTouchListener(null);
			item.setOnClickListener((View.OnClickListener) item.getParent());
		}

		// disable touch on tabtext
		view.findViewById(R.id.tabtxt).setOnTouchListener(null);

		progress.setText("Retrieving Stats");
		net.user_stats(settings.getString("username"));
		(new Thread(net)).start();

		return view;
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState)
	{
		savedInstanceState.putAll(settings);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		NetActive.inc();
	}

	@Override
	public void onPause()
	{
		NetActive.dec();
		super.onPause();
	}

	@Override
	public void onClick(final View view)
	{
		if (view.getId() == R.id.menu)
			openMenu(view);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = TAG;

		act.getMenuInflater().inflate(R.menu.options_userstats, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (act.lastContextMenu == TAG)
			return onOptionsItemSelected(item);
		else
			return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.userlookup) {
			(new StatsLookupDialog(act, handle)).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadStats(final JSONObject data)
	{
	try {
		TextView txt;
		long time;
		int valA, valB, valC, valD;

		// username
		txt = (TextView) act.findViewById(R.id.username);
		txt.setText(data.getString("username"));

		// Joined
		time = data.getLong("joined");

		txt = (TextView) act.findViewById(R.id.joined);
		txt.setText("Joined: " + (new PrettyDate(time)).dayFormat());

		// Last Activity
		time = data.getJSONObject("lastmove").getLong("genesis");
		time = Math.max(time, data.getJSONObject("lastmove").getLong("regular"));

		txt = (TextView) act.findViewById(R.id.last_activity);
		txt.setText(new PrettyDate(time).agoFormat());

		// PSR
		valA = data.getInt("gpsr");
		valB = data.getInt("rpsr");
		valC = (valA + valB) / 2;

		txt = (TextView) act.findViewById(R.id.gpsr);
		txt.setText("Genesis: " + String.valueOf(valA));
		txt = (TextView) act.findViewById(R.id.rpsr);
		txt.setText("Regular: " + String.valueOf(valB));
		txt = (TextView) act.findViewById(R.id.apsr);
		txt.setText("Average PSR: " + String.valueOf(valC));

		// Stats
		final JSONObject stats = data.getJSONObject("stats");

		final int arr[][] = new int[4][4];

		arr[GEN_RAN][WIN] = stats.getJSONObject("genesis_random").getInt("wins");
		arr[GEN_RAN][LOS] = stats.getJSONObject("genesis_random").getInt("losses");
		arr[GEN_RAN][TIE] = stats.getJSONObject("genesis_random").getInt("ties");
		arr[GEN_RAN][RES] = stats.getJSONObject("genesis_random").getInt("resigns");
		arr[GEN_INV][WIN] = stats.getJSONObject("genesis_invite").getInt("wins");
		arr[GEN_INV][LOS] = stats.getJSONObject("genesis_invite").getInt("losses");
		arr[GEN_INV][TIE] = stats.getJSONObject("genesis_invite").getInt("ties");
		arr[GEN_INV][RES] = stats.getJSONObject("genesis_invite").getInt("resigns");
		arr[REG_RAN][WIN] = stats.getJSONObject("regular_random").getInt("wins");
		arr[REG_RAN][LOS] = stats.getJSONObject("regular_random").getInt("losses");
		arr[REG_RAN][TIE] = stats.getJSONObject("regular_random").getInt("ties");
		arr[REG_RAN][RES] = stats.getJSONObject("regular_random").getInt("resigns");
		arr[REG_INV][WIN] = stats.getJSONObject("regular_invite").getInt("wins");
		arr[REG_INV][LOS] = stats.getJSONObject("regular_invite").getInt("losses");
		arr[REG_INV][TIE] = stats.getJSONObject("regular_invite").getInt("ties");
		arr[REG_INV][RES] = stats.getJSONObject("regular_invite").getInt("resigns");

		// Games
		valA = arr[GEN_RAN][WIN] + arr[GEN_RAN][LOS] + arr[GEN_RAN][TIE];
		valB = arr[GEN_INV][WIN] + arr[GEN_INV][LOS] + arr[GEN_INV][TIE];
		valC = valA + valB;

		txt = (TextView) act.findViewById(R.id.genesis_random_games);
		txt.setText("Random: " + String.valueOf(valA));
		txt = (TextView) act.findViewById(R.id.genesis_invite_games);
		txt.setText("Invite: " + String.valueOf(valB));
		txt = (TextView) act.findViewById(R.id.genesis_games);
		txt.setText("Genesis: " + String.valueOf(valC));

		valA = arr[REG_RAN][WIN] + arr[REG_RAN][LOS] + arr[REG_RAN][TIE];
		valB = arr[REG_INV][WIN] + arr[REG_INV][LOS] + arr[REG_INV][TIE];
		valD = valA + valB;

		txt = (TextView) act.findViewById(R.id.regular_random_games);
		txt.setText("Random: " + String.valueOf(valA));
		txt = (TextView) act.findViewById(R.id.regular_invite_games);
		txt.setText("Invite: " + String.valueOf(valB));
		txt = (TextView) act.findViewById(R.id.regular_games);
		txt.setText("Regular: " + String.valueOf(valD));

		txt = (TextView) act.findViewById(R.id.total_games);
		txt.setText("Total Games: " + String.valueOf(valC + valD));

		// Wins
		valC = arr[GEN_RAN][WIN] + arr[GEN_INV][WIN];
		valD = arr[REG_RAN][WIN] + arr[REG_INV][WIN];

		txt = (TextView) act.findViewById(R.id.genesis_random_wins);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][WIN]));
		txt = (TextView) act.findViewById(R.id.genesis_invite_wins);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][WIN]));
		txt = (TextView) act.findViewById(R.id.genesis_wins);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) act.findViewById(R.id.regular_random_wins);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][WIN]));
		txt = (TextView) act.findViewById(R.id.regular_invite_wins);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][WIN]));
		txt = (TextView) act.findViewById(R.id.regular_wins);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) act.findViewById(R.id.total_wins);
		txt.setText("Wins: " + String.valueOf(valC + valD));

		// Losses
		valC = arr[GEN_RAN][LOS] + arr[GEN_INV][LOS];
		valD = arr[REG_RAN][LOS] + arr[REG_INV][LOS];

		txt = (TextView) act.findViewById(R.id.genesis_random_losses);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][LOS]));
		txt = (TextView) act.findViewById(R.id.genesis_invite_losses);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][LOS]));
		txt = (TextView) act.findViewById(R.id.genesis_losses);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) act.findViewById(R.id.regular_random_losses);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][LOS]));
		txt = (TextView) act.findViewById(R.id.regular_invite_losses);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][LOS]));
		txt = (TextView) act.findViewById(R.id.regular_losses);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) act.findViewById(R.id.total_losses);
		txt.setText("Losses: " + String.valueOf(valC + valD));

		// Resigns
		valC = arr[GEN_RAN][RES] + arr[GEN_INV][RES];
		valD = arr[REG_RAN][RES] + arr[REG_INV][RES];

		txt = (TextView) act.findViewById(R.id.genesis_random_resigns);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][RES]));
		txt = (TextView) act.findViewById(R.id.genesis_invite_resigns);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][RES]));
		txt = (TextView) act.findViewById(R.id.genesis_resigns);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) act.findViewById(R.id.regular_random_resigns);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][RES]));
		txt = (TextView) act.findViewById(R.id.regular_invite_resigns);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][RES]));
		txt = (TextView) act.findViewById(R.id.regular_resigns);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) act.findViewById(R.id.total_resigns);
		txt.setText("Resigns: " + String.valueOf(valC + valD));

		// Ties
		valC = arr[GEN_RAN][TIE] + arr[GEN_INV][TIE];
		valD = arr[REG_RAN][TIE] + arr[REG_INV][TIE];

		txt = (TextView) act.findViewById(R.id.genesis_random_ties);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][TIE]));
		txt = (TextView) act.findViewById(R.id.genesis_invite_ties);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][TIE]));
		txt = (TextView) act.findViewById(R.id.genesis_ties);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) act.findViewById(R.id.regular_random_ties);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][TIE]));
		txt = (TextView) act.findViewById(R.id.regular_invite_ties);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][TIE]));
		txt = (TextView) act.findViewById(R.id.regular_ties);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) act.findViewById(R.id.total_ties);
		txt.setText("Draws: " + String.valueOf(valC + valD));
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
