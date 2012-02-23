package com.chess.genesis;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONTokener;

public class GameListLocalFrag extends GameListFrag implements OnClickListener, OnItemClickListener
{
	public final static String TAG = "GAMELISTLOCAL";

	private GameListAdapter gamelist_adapter;

	public final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			switch (msg.what) {
			case DeleteLocalDialog.MSG:
			case RenameGameDialog.MSG:
				gamelist_adapter.update();
				break;
			case NewLocalGameDialog.MSG:
				final GameDataDB db = new GameDataDB(act);
				final Bundle bundle = (Bundle) msg.obj;

				final int gametype2 = bundle.getInt("gametype");
				final int gameopp = bundle.getInt("opponent");
				String gamename = bundle.getString("name");
				if (gamename.length() < 1)
					gamename = "untitled";

				final Bundle gamedata = db.newLocalGame(gamename, gametype2, gameopp);
				db.close();

				loadGame(gamedata);
				break;
			}
		}
	};

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		initBaseContentFrag();

		final View view = inflater.inflate(R.layout.fragment_gamelist_local, null);

		// set list adapters
		gamelist_adapter = new GameListAdapter(getActivity(), Enums.LOCAL_GAME, Enums.YOUR_TURN);

		final ListView gamelist_view = (ListView) view.findViewById(R.id.game_list);
		gamelist_view.setAdapter(gamelist_adapter);
		gamelist_view.setOnItemClickListener(this);

		// set empty view item
		final View empty = gamelist_adapter.getEmptyView(getActivity());
		((ViewGroup) gamelist_view.getParent()).addView(empty);
		gamelist_view.setEmptyView(empty);

		registerForContextMenu(gamelist_view);

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		gamelist_adapter.update();
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
		else if (v.getId() == R.id.menu)
			openMenu(v);
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		final Bundle data = (Bundle) parent.getItemAtPosition(position);

		loadGame(data);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = TAG;

		if (v.getId() == R.id.menu)
			act.getMenuInflater().inflate(R.menu.options_gamelist_local, menu);
		else
			act.getMenuInflater().inflate(R.menu.context_gamelist_local, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (act.lastContextMenu != TAG)
			return super.onContextItemSelected(item);

		switch (item.getItemId()) {
		case R.id.new_game:
		case R.id.import_game:
			return onOptionsItemSelected(item);
		}

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final Bundle bundle = (Bundle) gamelist_adapter.getItem((int) info.id);

		switch (item.getItemId()) {
		case R.id.delete_game:
			(new DeleteLocalDialog(act, handle, Integer.valueOf(bundle.getString("id")))).show();
			break;
		case R.id.rename_game:
			(new RenameGameDialog(act, handle, Integer.valueOf(bundle.getString("id")), bundle.getString("name"))).show();
			break;
		case R.id.share_game:
			sendGame(bundle);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.new_game:
			(new NewLocalGameDialog(act, handle)).show();
			break;
		case R.id.import_game:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent = intent.addCategory(Intent.CATEGORY_OPENABLE).setType("text/*");
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
	try {
			startActivityForResult(intent, Enums.IMPORT_GAME);
	} catch (ActivityNotFoundException e) {
			Toast.makeText(act, "No File Manager Installed", Toast.LENGTH_LONG).show();
	}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void updateGameList()
	{
		gamelist_adapter.update();
	}

	public void recieveGame(final Intent data)
	{
	try {
		final String str = FileUtils.readFile(data.getData().getPath());
		final JSONObject json = (JSONObject) (new JSONTokener(str)).nextValue();
		final Bundle game = GameParser.parse(json);
		final GameDataDB db = new GameDataDB(act);

		db.addLocalGame(game);
		db.close();
		gamelist_adapter.update();
	} catch (FileNotFoundException e) {
		Toast.makeText(act, "File Not Found", Toast.LENGTH_LONG).show();
	} catch (IOException e) {
		Toast.makeText(act, "Error Reading File", Toast.LENGTH_LONG).show();
	} catch (JSONException e) {
		Toast.makeText(act, "Not A Game Data File", Toast.LENGTH_LONG).show();
	} catch (ClassCastException e) {
		Toast.makeText(act, "Not A Game Data File", Toast.LENGTH_LONG).show();
	}
	}
}
