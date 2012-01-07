package com.chess.genesis;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class UserStats extends Activity implements OnLongClickListener
{
	private final static int GEN_RAN = 0;
	private final static int GEN_INV = 1;
	private final static int REG_RAN = 2;
	private final static int REG_INV = 3;

	private final static int WIN = 0;
	private final static int LOS = 1;
	private final static int TIE = 2;
	private final static int RES = 3;

	private Context context;
	private Bundle settings;
	private ProgressMsg progress;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;
		try {
			if (json.getString("result").equals("error")) {
				progress.remove();
				Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
				return;
			}
			switch (msg.what) {
			case NetworkClient.USER_STATS:
				loadStats(json);
				progress.remove();
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;

		// set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(R.layout.activity_userstats);

		settings = (savedInstanceState != null)? savedInstanceState : getIntent().getExtras();

		final NetworkClient net = new NetworkClient(this, handle);
		progress = new ProgressMsg(this);

		final ImageView button = (ImageView) findViewById(R.id.topbar);
		button.setOnLongClickListener(this);

		final RobotoText txt = (RobotoText) findViewById(R.id.username);
		txt.setText(settings.getString("username"));

		final int[] list = new int[]{R.id.apsr, R.id.total_games, R.id.total_wins,
			R.id.total_losses, R.id.total_resigns, R.id.total_ties};
		for (int id : list) {
			final TabText item = (TabText) findViewById(id);
			item.setOnTouchListener(null);
			item.setOnClickListener((View.OnClickListener) item.getParent());
		}

		progress.setText("Retrieving Stats");
		net.user_stats(settings.getString("username"));
		(new Thread(net)).start();
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
		AdsHandler.run(this);
	}

	@Override
	public void onPause()
	{
		NetActive.dec();
		super.onPause();
	}

	public boolean onLongClick(final View v)
	{
		if (v.getId() == R.id.topbar) {
			finish();
			return true;
		}
		return false;
	}

	private void loadStats(final JSONObject data)
	{
	try {
		TextView txt;
		long time;
		int valA, valB, valC, valD;

		// Joined
		time = data.getLong("joined");

		txt = (TextView) findViewById(R.id.joined);
		txt.setText("Joined: " + (new PrettyDate(time)).dayFormat());

		// Last Activity
		time = data.getJSONObject("lastmove").getLong("genesis");
		time = Math.max(time, data.getJSONObject("lastmove").getLong("regular"));

		txt = (TextView) findViewById(R.id.last_activity);
		txt.setText((new PrettyDate(time)).agoFormat());

		// PSR
		valA = data.getInt("gpsr");
		valB = data.getInt("rpsr");
		valC = (valA + valB) / 2;

		txt = (TextView) findViewById(R.id.gpsr);
		txt.setText("Genesis: " + String.valueOf(valA));
		txt = (TextView) findViewById(R.id.rpsr);
		txt.setText("Regular: " + String.valueOf(valB));
		txt = (TextView) findViewById(R.id.apsr);
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

		txt = (TextView) findViewById(R.id.genesis_random_games);
		txt.setText("Random: " + String.valueOf(valA));
		txt = (TextView) findViewById(R.id.genesis_invite_games);
		txt.setText("Invite: " + String.valueOf(valB));
		txt = (TextView) findViewById(R.id.genesis_games);
		txt.setText("Genesis: " + String.valueOf(valC));

		valA = arr[REG_RAN][WIN] + arr[REG_RAN][LOS] + arr[REG_RAN][TIE];
		valB = arr[REG_INV][WIN] + arr[REG_INV][LOS] + arr[REG_INV][TIE];
		valD = valA + valB;

		txt = (TextView) findViewById(R.id.regular_random_games);
		txt.setText("Random: " + String.valueOf(valA));
		txt = (TextView) findViewById(R.id.regular_invite_games);
		txt.setText("Invite: " + String.valueOf(valB));
		txt = (TextView) findViewById(R.id.regular_games);
		txt.setText("Regular: " + String.valueOf(valD));

		txt = (TextView) findViewById(R.id.total_games);
		txt.setText("Total Games: " + String.valueOf(valC + valD));

		// Wins
		valC = arr[GEN_RAN][WIN] + arr[GEN_INV][WIN];
		valD = arr[REG_RAN][WIN] + arr[REG_INV][WIN];

		txt = (TextView) findViewById(R.id.genesis_random_wins);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][WIN]));
		txt = (TextView) findViewById(R.id.genesis_invite_wins);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][WIN]));
		txt = (TextView) findViewById(R.id.genesis_wins);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) findViewById(R.id.regular_random_wins);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][WIN]));
		txt = (TextView) findViewById(R.id.regular_invite_wins);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][WIN]));
		txt = (TextView) findViewById(R.id.regular_wins);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) findViewById(R.id.total_wins);
		txt.setText("Wins: " + String.valueOf(valC + valD));

		// Losses
		valC = arr[GEN_RAN][LOS] + arr[GEN_INV][LOS];
		valD = arr[REG_RAN][LOS] + arr[REG_INV][LOS];

		txt = (TextView) findViewById(R.id.genesis_random_losses);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][LOS]));
		txt = (TextView) findViewById(R.id.genesis_invite_losses);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][LOS]));
		txt = (TextView) findViewById(R.id.genesis_losses);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) findViewById(R.id.regular_random_losses);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][LOS]));
		txt = (TextView) findViewById(R.id.regular_invite_losses);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][LOS]));
		txt = (TextView) findViewById(R.id.regular_losses);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) findViewById(R.id.total_losses);
		txt.setText("Losses: " + String.valueOf(valC + valD));

		// Resigns
		valC = arr[GEN_RAN][RES] + arr[GEN_INV][RES];
		valD = arr[REG_RAN][RES] + arr[REG_INV][RES];

		txt = (TextView) findViewById(R.id.genesis_random_resigns);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][RES]));
		txt = (TextView) findViewById(R.id.genesis_invite_resigns);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][RES]));
		txt = (TextView) findViewById(R.id.genesis_resigns);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) findViewById(R.id.regular_random_resigns);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][RES]));
		txt = (TextView) findViewById(R.id.regular_invite_resigns);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][RES]));
		txt = (TextView) findViewById(R.id.regular_resigns);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) findViewById(R.id.total_resigns);
		txt.setText("Resigns: " + String.valueOf(valC + valD));

		// Ties
		valC = arr[GEN_RAN][TIE] + arr[GEN_INV][TIE];
		valD = arr[REG_RAN][TIE] + arr[REG_INV][TIE];

		txt = (TextView) findViewById(R.id.genesis_random_ties);
		txt.setText("Random: " + String.valueOf(arr[GEN_RAN][TIE]));
		txt = (TextView) findViewById(R.id.genesis_invite_ties);
		txt.setText("Invite: " + String.valueOf(arr[GEN_INV][TIE]));
		txt = (TextView) findViewById(R.id.genesis_ties);
		txt.setText("Genesis: " + String.valueOf(valC));
		txt = (TextView) findViewById(R.id.regular_random_ties);
		txt.setText("Random: " + String.valueOf(arr[REG_RAN][TIE]));
		txt = (TextView) findViewById(R.id.regular_invite_ties);
		txt.setText("Invite: " + String.valueOf(arr[REG_INV][TIE]));
		txt = (TextView) findViewById(R.id.regular_ties);
		txt.setText("Regular: " + String.valueOf(valD));
		txt = (TextView) findViewById(R.id.total_ties);
		txt.setText("Draws: " + String.valueOf(valC + valD));
	} catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
