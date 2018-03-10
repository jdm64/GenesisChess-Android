/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis.activity;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.util.*;
import java.io.*;
import org.json.*;

public class GameListLocalFrag extends GameListFrag implements OnItemClickListener, Handler.Callback
{
	public final static String TAG = "GAMELISTLOCAL";

	private final Handler handle = new Handler(this);
	private GameListAdapter gamelist_adapter;

	@Override
	public boolean handleMessage(final Message msg)
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
		return true;
	}

	@Override
	public String getBTag()
	{
		return TAG;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		initBaseContentFrag(container);

		final View view = inflater.inflate(R.layout.fragment_gamelist_local, null);

		final ListView gamelist_view = view.findViewById(R.id.game_list);
		gamelist_view.setOnItemClickListener(this);

		gamelist_adapter = new GameListAdapter(gamelist_view, Enums.LOCAL_GAME, Enums.YOUR_TURN);

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

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.topbar_plus)
			NewLocalGameDialog.create(handle).show(getFragmentManager(), "");
		else if (v.getId() == R.id.menu)
			openMenu(v);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = getBTag();
		act.getMenuInflater().inflate(v.getId() == R.id.menu? R.menu.options_gamelist_local : R.menu.context_gamelist_local, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (!act.lastContextMenu.equals(getBTag()))
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
			DeleteLocalDialog.create(handle, Integer.parseInt(bundle.getString("id"))).show(getFragmentManager(), "");
			break;
		case R.id.rename_game:
			RenameGameDialog.create(handle, Integer.parseInt(bundle.getString("id")), bundle.getString("name")).show(getFragmentManager(), "");
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
			NewLocalGameDialog.create(handle).show(getFragmentManager(), "");
			break;
		case R.id.import_game:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent = intent.addCategory(Intent.CATEGORY_OPENABLE).setType("text/*");
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
	try {
			startActivityForResult(intent, Enums.IMPORT_GAME);
	} catch (final ActivityNotFoundException e) {
			Toast.makeText(act, "No File Manager Installed", Toast.LENGTH_LONG).show();
	}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void updateGameList()
	{
		gamelist_adapter.update();
	}

	public void recieveGame(final Intent data)
	{
	try {
		if (data == null || data.getDataString() == null) {
			Toast.makeText(act, "Intent has no data", Toast.LENGTH_LONG).show();
			return;
		}

		final String str = FileUtils.readFile(data.getDataString());
		final JSONObject json = (JSONObject) new JSONTokener(str).nextValue();
		final Bundle game = GameParser.parse(json);
		final GameDataDB db = new GameDataDB(act);

		db.addLocalGame(game);
		db.close();
		gamelist_adapter.update();
	} catch (final FileNotFoundException e) {
		Toast.makeText(act, "File Not Found", Toast.LENGTH_LONG).show();
	} catch (final IOException e) {
		Toast.makeText(act, "Error Reading File", Toast.LENGTH_LONG).show();
	} catch (final JSONException | ClassCastException e) {
		Toast.makeText(act, "Not A Game Data File", Toast.LENGTH_LONG).show();
	}
	}
}
