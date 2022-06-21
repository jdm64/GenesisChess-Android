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
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.view.*;

public class MainMenuFrag extends AbstractActivityFrag implements OnTouchListener, OnClickListener
{
	@Override
	public boolean handleMessage(final Message msg)
	{
		if (msg.what == LogoutConfirm.MSG) {
			new PrefEdit(act)
				.putBool(R.array.pf_isLoggedIn)
				.putString(R.array.pf_username)
				.putString(R.array.pf_passhash)
				.putLong(R.array.pf_lastgamesync)
				.putLong(R.array.pf_lastmsgsync)
				.commit();

			updateLoggedInView();
		}
		return true;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_mainmenu, container, false);

		// setup click listeners
		final int[] list = new int[]{R.id.local_game, R.id.online_game,
			R.id.user_stats, R.id.howtoplay, R.id.login, R.id.settings, };
		for (final int element : list) {
			final View button = view.findViewById(element);
			button.setOnClickListener(this);
			button.setOnTouchListener(this);
		}

		final TextView textView = view.findViewById(R.id.version);
		textView.setText(BuildConfig.VERSION_NAME);

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateLoggedInView();
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event)
	{
		switch (v.getId()) {
		case R.id.local_game:
		case R.id.online_game:
		case R.id.user_stats:
		case R.id.howtoplay:
		case R.id.login:
		case R.id.settings:
			v.setBackgroundColor((event.getAction() == MotionEvent.ACTION_DOWN)? MColors.BLUE_LIGHT_500 : MColors.CLEAR);
			break;
		}
		return false;
	}

	@Override
	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.howtoplay:
			Uri uri = Uri.parse("http://goo.gl/eyLYY");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.settings:
			startActivity(new Intent(act, Settings.class));
			break;
		default:
			onClickPhone(v.getId());
			break;
		}
	}

	private void onClickPhone(final int viewId)
	{
		Intent intent;

		switch (viewId) {
		case R.id.local_game:
			startActivity(new Intent(act, GameListLocal.class));
			break;
		case R.id.online_game:
			if (!Pref.getBool(act, R.array.pf_isLoggedIn)) {
				startActivityForResult(new Intent(act, Login.class), Enums.ONLINE_LIST);
				return;
			}
			startActivity(new Intent(act, GameListOnline.class));
			break;
		case R.id.user_stats:
			final Pref pref = new Pref(act);
			if (!pref.getBool(R.array.pf_isLoggedIn)) {
				startActivityForResult(new Intent(act, Login.class), Enums.USER_STATS);
				return;
			}
			intent = new Intent(act, UserStats.class);
			intent.putExtra(pref.key(R.array.pf_username), pref.getString(R.array.pf_username));
			startActivity(intent);
			break;
		case R.id.login:
			startActivity(new Intent(act, Login.class));
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_mainmenu, menu);

		if (!new Pref(act).getBool(R.array.pf_isLoggedIn)) {
			menu.removeItem(R.id.logout);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.logout) {
			if (Pref.getBool(act, R.array.pf_isLoggedIn))
				LogoutConfirm.create(new Handler(this)).show(getFragmentManager(), "");
			else
				Toast.makeText(act, "Already Logged Out", Toast.LENGTH_LONG).show();
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void updateLoggedInView()
	{
		String welcome = "";
		int welcomeVis = View.GONE, loginVis = View.VISIBLE;

		final Pref pref = new Pref(act);
		if (pref.getBool(R.array.pf_isLoggedIn)) {
			welcome = "Welcome " + pref.getString(R.array.pf_username);
			welcomeVis = View.VISIBLE;
			loginVis = View.GONE;
		}

		TextView text = act.findViewById(R.id.welcome);
		text.setVisibility(welcomeVis);
		text.setText(welcome);

		text = act.findViewById(R.id.login_txt);
		text.setVisibility(loginVis);

		final MyImageView button = act.findViewById(R.id.login);
		button.setVisibility(loginVis);

		act.invalidateOptionsMenu();
	}
}
