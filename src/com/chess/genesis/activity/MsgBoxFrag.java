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
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MsgBoxFrag extends BaseContentFrag implements OnClickListener
{
	public final static String TAG = "MSGBOX";

	private MsgListAdapter msglist_adapter;
	private ListView msglist_view;
	private NetworkClient net;
	private ProgressMsg progress;
	private Bundle settings;
	private String gameid;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			final JSONObject json = (JSONObject) msg.obj;

			try {
				if (json.getString("result").equals("error")) {
					progress.remove();
					Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return;
				}
				switch (msg.what) {
				case NetworkClient.SUBMIT_MSG:
					final EditText txt = (EditText) act.findViewById(R.id.new_msg);
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

		final View view = inflater.inflate(R.layout.fragment_msgbox, container, false);

		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act);

		// restore settings
		settings = (savedInstanceState != null)?
			savedInstanceState : getArguments();
		gameid = settings.getString("gameid");

		final View btn = view.findViewById(R.id.submit_msg);
		btn.setOnClickListener(this);

		// set list adapters
		msglist_adapter = new MsgListAdapter(act, gameid);

		msglist_view = (ListView) view.findViewById(R.id.msg_list);
		msglist_view.setAdapter(msglist_adapter);

		// set empty view item
		final View empty = msglist_adapter.getEmptyView(act);
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
		NetActive.dec();
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		msglist_adapter.close();
		super.onDestroy();
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.submit_msg) {
			progress.setText("Sending Message");

			final EditText txt = (EditText) act.findViewById(R.id.new_msg);
			final String msg = txt.getText().toString().trim();

			if (msg.length() < 1)
				return;
			net.submit_msg(gameid, msg);
			(new Thread(net)).start();
		} else if (v.getId() == R.id.menu) {
			openMenu(v);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		act.lastContextMenu = TAG;

		act.getMenuInflater().inflate(R.menu.options_msgbox, menu);
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
		(new Thread(net)).start();
		db.close();
	}

	private void saveMsgs(final JSONObject data)
	{
	try {
		final JSONArray msgs = data.getJSONArray("msglist");
		final GameDataDB db = new GameDataDB(act);

		for (int i = 0; i < msgs.length(); i++) {
			final JSONObject item = msgs.getJSONObject(i);
			db.insertMsg(item);
		}
		db.setMsgsRead(gameid);
		db.close();
	}  catch (JSONException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}