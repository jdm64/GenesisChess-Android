/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
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

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;

public class GameListPage extends Fragment
{
	private static int curContextType;

	private GameListAdapter list;

	public static Fragment newInstance(int type)
	{
		GameListPage frag = new GameListPage();
		Bundle data = new Bundle();
		data.putInt("type", type);
		frag.setArguments(data);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final GameListAdapter adapter = getAdapter();
		final FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.gamelist_listview, container, false);
		final ListView listview = (ListView) layout.getChildAt(0);
		final View empty = adapter.getEmptyView(getActivity());
		final GameListOnlineFrag parent = (GameListOnlineFrag) getFragmentManager().findFragmentByTag(GameListOnlineFrag.TAG);

		layout.addView(empty, 1);
		listview.setEmptyView(empty);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(parent);
		registerForContextMenu(listview);

		return layout;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		curContextType = getPageType();

		final MenuInflater inflater = getActivity().getMenuInflater();
		switch (getPageType()) {
		case Enums.THEIR_PAGE:
			final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			final Bundle bundle = (Bundle) list.getItem((int) info.id);

			if (bundle.getString("idle").equals("1")) {
				inflater.inflate(R.menu.context_gamelist_online_nudge, menu);
				break;
			}
		case Enums.YOUR_PAGE:
			inflater.inflate(R.menu.context_gamelist_online, menu);
			break;
		case Enums.ARCHIVE_PAGE:
			inflater.inflate(R.menu.context_gamelist_archive, menu);
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		if (getPageType() != curContextType)
			return super.onContextItemSelected(item);

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final Bundle bundle = (Bundle) list.getItem((int) info.id);
		final GameListOnlineFrag frag = (GameListOnlineFrag) getFragmentManager().findFragmentByTag(GameListOnlineFrag.TAG);
		final Handler handle = new Handler(frag);
		final FragmentActivity act = getActivity();

		switch (item.getItemId()) {
		case R.id.delete_game:
			new DeleteArchiveDialog(act, handle, bundle.getString("gameid")).show();
			break;
		case R.id.local_copy:
			new CopyGameConfirm(act, bundle.getString("gameid"), list.getType()).show();
			break;
		case R.id.rematch:
			final String username = list.getExtras().getString("username");
			final String opponent = username.equals(bundle.getString("white"))?
				bundle.getString("black") : bundle.getString("white");
			new RematchConfirm(act, handle, opponent).show();
			break;
		case R.id.nudge:
			new NudgeConfirm(act, handle, bundle.getString("gameid")).show();
			break;
		case R.id.share_game:
			frag.sendGame(bundle);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void onDestroy()
	{
		if (list != null)
			list.close();
		super.onDestroy();
	}

	public void update()
	{
		if (list == null)
			return;
		list.update();
	}

	private GameListAdapter getAdapter()
	{
		if (list != null)
			return list;

		int type = Enums.ONLINE_GAME, yourmove = Enums.YOUR_TURN;

		switch (getPageType()) {
		case Enums.THEIR_PAGE:
			yourmove = Enums.THEIR_TURN;
			break;
		case Enums.YOUR_PAGE:
			// already initialized
			break;
		case Enums.ARCHIVE_PAGE:
			type = Enums.ARCHIVE_GAME;
			break;
		}
		list = new GameListAdapter(getActivity(), type, yourmove);
		return list;
	}

	private int getPageType()
	{
		return getArguments().getInt("type", Enums.YOUR_PAGE);
	}
}
