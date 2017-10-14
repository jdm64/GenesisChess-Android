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

import android.os.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import org.json.*;

public class MsgBoxFrag extends BaseContentFrag implements Handler.Callback
{
	private final static String TAG = "MSGBOX";

	private final Handler handle = new Handler(this);
	private MsgListAdapter msglist_adapter;
	private ListView msglist_view;
	private NetworkClient net;
	private ProgressMsg progress;
	private Bundle settings;
	private String gameid;

	@Override
	public boolean handleMessage(final Message msg)
	{
		final JSONObject json = (JSONObject) msg.obj;

	try {
		if (json.getString("result").equals("error")) {
			progress.dismiss();
			Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
			return true;
		}
		switch (msg.what) {
		case NetworkClient.SUBMIT_MSG:
			final EditText txt = act.findViewById(R.id.new_msg);
			txt.setText("");

			updateMsgList();
			break;
		case NetworkClient.SYNC_MSGS:
			saveMsgs(json);
			msglist_adapter.update();
			msglist_view.setSelection(msglist_view.getCount() - 1);
			GenesisNotifier.clearNotification(act, GenesisNotifier.NEWMGS_NOTE);
			progress.dismiss();
			break;
		}
		return true;
	} catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	@Override
	public String getBTag()
	{
		return TAG;
	}

	@Override
	public void onCreate(final Bundle data)
	{
		super.onCreate(data);
		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		initBaseContentFrag(container);

		final View view = inflater.inflate(R.layout.fragment_msgbox, container, false);

		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getArguments();
		gameid = settings.getString("gameid");

		final View btn = view.findViewById(R.id.submit_msg);
		btn.setOnClickListener(this);

		// disable touch on tabtext
		view.findViewById(R.id.tabtxt).setOnTouchListener(null);

		// set list adapters
		msglist_adapter = new MsgListAdapter(act, gameid);

		msglist_view = view.findViewById(R.id.msg_list);
		msglist_view.setAdapter(msglist_adapter);

		// set empty view item
		final View empty = MsgListAdapter.getEmptyView(act);
		((ViewGroup) msglist_view.getParent()).addView(empty);
		msglist_view.setEmptyView(empty);

		// scroll to bottom
		msglist_view.setSelection(msglist_view.getCount() - 1);

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
		updateMsgList();
	}

	@Override
	public void onPause()
	{
		NetActive.dec(getActivity());
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		msglist_adapter.close();
		super.onDestroy();
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.submit_msg) {
			progress.setText("Sending Message");

			final EditText txt = act.findViewById(R.id.new_msg);
			final String msg = txt.getText().toString().trim();

			if (msg.length() < 1)
				return;
			net.submit_msg(gameid, msg);
			new Thread(net).start();
		} else if (v.getId() == R.id.menu) {
			openMenu(v);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = getBTag();
		act.getMenuInflater().inflate(R.menu.options_msgbox, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.resync) {
			updateMsgList();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateMsgList()
	{
		progress.setText("Updating Messages");
		final GameDataDB db = new GameDataDB(act);
		net.sync_msgs(db.getNewestMsg());
		new Thread(net).start();
		db.close();
	}

	private void saveMsgs(final JSONObject data)
	{
	try {
		final JSONArray msgs = data.getJSONArray("msglist");
		final GameDataDB db = new GameDataDB(act);

		for (int i = 0, len = msgs.length(); i < len; i++) {
			final JSONObject item = msgs.getJSONObject(i);
			db.insertMsg(item);
		}
		db.setMsgsRead(gameid);
		db.close();
	}  catch (final JSONException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}
}
