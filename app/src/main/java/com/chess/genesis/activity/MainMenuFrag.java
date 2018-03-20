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
import android.support.v4.app.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.data.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.util.*;
import com.chess.genesis.view.*;

public class MainMenuFrag extends AbstractActivityFrag implements OnTouchListener, OnClickListener, OnGlobalLayoutListener
{
	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what) {
		case LogoutConfirm.MSG:
			final PrefEdit pref = new PrefEdit(act);

			pref.putBool(R.array.pf_isLoggedIn);
			pref.putString(R.array.pf_username);
			pref.putString(R.array.pf_passhash);
			pref.putLong(R.array.pf_lastgamesync);
			pref.putLong(R.array.pf_lastmsgsync);
			pref.commit();

			updateLoggedInView();
			break;
		}
		return true;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_mainmenu, container, false);

		// setup click listeners
		final int list[] = new int[]{R.id.local_game, R.id.online_game,
			R.id.user_stats, R.id.howtoplay, R.id.likefacebook,
			R.id.login, R.id.settings, R.id.feedback, R.id.googleplus};
		for (final int element : list) {
			final View button = view.findViewById(element);
			button.setOnClickListener(this);
			button.setOnTouchListener(this);
		}

		final TextView textView = view.findViewById(R.id.version);
		textView.setText(BuildConfig.VERSION_NAME);

		// create layout listner for resizing the button text
		final View v = view.findViewById(R.id.online_game);
		final ViewTreeObserver vto = v.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(this);

		return view;
	}

	@Override
	public void onGlobalLayout()
	{
		resizeButtonText(getView());

		// remove layout listener once text was resized
		final View v = getView().findViewById(R.id.online_game);
		final ViewTreeObserver vto = v.getViewTreeObserver();
		vto.removeGlobalOnLayoutListener(this);
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
		case R.id.feedback:
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
		case R.id.likefacebook:
			uri = Uri.parse("http://goo.gl/tQVOh");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.googleplus:
			uri = Uri.parse("https://plus.google.com/110270264298991399402");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.feedback:
			uri = Uri.parse(getResources().getString(R.string.feedback_url));
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.settings:
			startActivity(new Intent(act, Settings.class));
			break;
		default:
			if (isTablet)
				onClickTablet(v.getId());
			else
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

	private void onClickTablet(final int viewId)
	{
		// pop everything from stack
		for (int i = 0, len = fragMan.getBackStackEntryCount(); i < len; i++)
			fragMan.popBackStack();

		final FragmentIntent fintent = new FragmentIntent();

		switch (viewId) {
		case R.id.local_game:
			fintent.setFrag(R.id.panel01, new GameListLocalFrag());
			break;
		case R.id.online_game:
			if (!Pref.getBool(act, R.array.pf_isLoggedIn)) {
				final LoginFrag frag = new LoginFrag();
				frag.setCallBack(Enums.ONLINE_LIST);
				fintent.setFrag(R.id.panel02, frag);
			} else {
				fintent.setFrag(R.id.panel01, new GameListOnlineFrag());
			}
			break;
		case R.id.user_stats:
			final Pref pref = new Pref(act);
			if (!pref.getBool(R.array.pf_isLoggedIn)) {
				final LoginFrag frag = new LoginFrag();
				frag.setCallBack(Enums.USER_STATS);
				fintent.setFrag(R.id.panel02, frag);
			} else {
				final BaseContentFrag frag = new UserStatsFrag();
				final Bundle bundle = new Bundle();
				bundle.putString(pref.key(R.array.pf_username), pref.getString(R.array.pf_username));
				frag.setArguments(bundle);

				fintent.setFrag(R.id.panel02, frag);
			}
			break;
		case R.id.login:
			fintent.setFrag(R.id.panel02, new LoginFrag());
			break;
		}
		fintent.loadFrag(fragMan);
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
		switch (item.getItemId()) {
		case R.id.logout:
			if (Pref.getBool(act, R.array.pf_isLoggedIn))
				new LogoutConfirm(act, new Handler(this)).show();
			else
				Toast.makeText(act, "Already Logged Out", Toast.LENGTH_LONG).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void startFragment(final int fragId)
	{
		Fragment frag;

		fragMan.popBackStack();
		switch (fragId) {
		case Enums.ONLINE_LIST:
			frag = new GameListOnlineFrag();
			break;
		case Enums.USER_STATS:
			frag = new UserStatsFrag();
			final Pref pref = new Pref(act);
			final Bundle bundle = new Bundle();
			bundle.putString(pref.key(R.array.pf_username), pref.getString(R.array.pf_username));
			frag.setArguments(bundle);
			break;
		default:
			updateLoggedInView();
			return;
		}
		fragMan.beginTransaction()
		.replace(R.id.panel01, frag, frag.getClass().getName())
		.addToBackStack(frag.getClass().getName()).commit();
	}

	private static void resizeButtonText(final View view)
	{
		final int list[] = new int[]{R.id.local_game_txt, R.id.online_game_txt,
			R.id.archive_game_txt, R.id.howtoplay_txt, R.id.login_txt,
			R.id.settings_txt, R.id.feedback_txt};

		final String[] stList = new String[list.length];
		TextView txt = null;
		for (int i = 0; i < list.length; i++) {
			txt = view.findViewById(list[i]);
			stList[i] = (String) txt.getText();
		}
		if (txt == null)
			return;

		float width = view.getWidth();
		width *= 0.9 / 3;

		final float txtSize = TextUtil.maxTextWidth(stList, txt.getPaint(), width);
		for (final int element : list) {
			txt = view.findViewById(element);
			txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
		}
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
