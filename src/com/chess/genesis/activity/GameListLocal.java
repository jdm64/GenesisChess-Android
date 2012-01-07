package com.chess.genesis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONTokener;

public class GameListLocal extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener
{
	private Context context;
	private GameListAdapter gamelist_adapter;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case DeleteLocalDialog.MSG:
			case RenameGameDialog.MSG:
				gamelist_adapter.update();
				break;
			case NewLocalGameDialog.MSG:
				final GameDataDB db = new GameDataDB(context);
				final Bundle bundle = (Bundle) msg.obj;

				final int gametype2 = bundle.getInt("gametype");
				final int gameopp = bundle.getInt("opponent");
				String gamename = bundle.getString("name");
				if (gamename.length() < 1)
					gamename = "untitled";

				final Intent intent;
				if (gametype2 == Enums.GENESIS_CHESS)
					intent = new Intent(context, GenGame.class);
				else
					intent = new Intent(context, RegGame.class);
				intent.putExtras(db.newLocalGame(gamename, gametype2, gameopp));
				db.close();

				startActivity(intent);
				break;
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(R.layout.activity_gamelist_local);

		// set click listeners
		ImageView button = (ImageView) findViewById(R.id.topbar_genesis);
		button.setOnLongClickListener(this);
		button = (ImageView) findViewById(R.id.topbar_plus);
		button.setOnClickListener(this);

		// set list adapters
		gamelist_adapter = new GameListAdapter(this, Enums.LOCAL_GAME, Enums.YOUR_TURN);

		final ListView gamelist_view = (ListView) findViewById(R.id.game_list);
		gamelist_view.setAdapter(gamelist_adapter);
		gamelist_view.setOnItemClickListener(this);

		// set empty view item
		final View empty = gamelist_adapter.getEmptyView(this);
		((ViewGroup) gamelist_view.getParent()).addView(empty);
		gamelist_view.setEmptyView(empty);

		registerForContextMenu(gamelist_view);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		gamelist_adapter.update();
		AdsHandler.run(this);
	}

	@Override
	public void onDestroy()
	{
		gamelist_adapter.close();
		super.onDestroy();
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.topbar_plus)
			(new NewLocalGameDialog(v.getContext(), handle)).show();
	}

	public boolean onLongClick(final View v)
	{
		if (v.getId() == R.id.topbar_genesis) {
			finish();
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (resultCode == RESULT_CANCELED || data == null)
			return;

		recieveGame(data);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.options_gamelist_local, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.import_game) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent = intent.addCategory(Intent.CATEGORY_OPENABLE).setType("text/*");
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

			startActivityForResult(intent, 1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Bundle data = (Bundle) parent.getItemAtPosition(position);
		final Intent intent;

		if (Integer.valueOf(data.getString("gametype")) == Enums.GENESIS_CHESS)
			intent = new Intent(this, GenGame.class);
		else
			intent = new Intent(this, RegGame.class);

		intent.putExtras(data);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.context_gamelist_local, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final Bundle bundle = (Bundle) gamelist_adapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			(new DeleteLocalDialog(this, handle, Integer.valueOf(bundle.getString("id")))).show();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(this, handle, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
			break;
		case R.id.share_game:
			sendGame(bundle);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void recieveGame(final Intent data)
	{
	try {
		final String str = FileUtils.readFile(data.getData().getPath());
		final JSONObject json = (JSONObject) (new JSONTokener(str)).nextValue();
		final Bundle game = GameParser.parse(json);
		final GameDataDB db = new GameDataDB(this);

		db.addLocalGame(game);
		db.close();
		gamelist_adapter.update();
	} catch (FileNotFoundException e) {
		Toast.makeText(this, "File Not Found", Toast.LENGTH_LONG).show();
	} catch (IOException e) {
		Toast.makeText(this, "Error Reading File", Toast.LENGTH_LONG).show();
	} catch (JSONException e) {
		Toast.makeText(this, "File Not JSON Object", Toast.LENGTH_LONG).show();
	}
	}

	private void sendGame(final Bundle gamedata)
	{
	try {
		final String gamestr = GameParser.export(gamedata).toString();
		final Intent intent = new Intent(Intent.ACTION_SEND);
		final String filename = "genesischess-" + gamedata.getString("name") + ".txt";
		final Uri uri = FileUtils.writeFile(filename, gamestr);

		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType("application/json");
		startActivity(intent);
	} catch (JSONException e) {
		Toast.makeText(this, "Corrupt Game Data", Toast.LENGTH_LONG).show();
	} catch (FileNotFoundException e) {
		Toast.makeText(this, "File Not Found", Toast.LENGTH_LONG).show();
	} catch (IOException e) {
		Toast.makeText(this, "Error Reading File", Toast.LENGTH_LONG).show();
	}
	}
}
