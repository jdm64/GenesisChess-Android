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
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;
import org.json.*;

public class RegisterFrag extends BaseContentFrag implements Handler.Callback
{
	private final static String TAG = "REGISTER";

	private final Handler handle = new Handler(this);
	private NetworkClient net;
	private ProgressMsg progress;

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what) {
		case NetworkClient.REGISTER:
			final JSONObject json = (JSONObject) msg.obj;

			try {
				if (json.getString("result").equals("error")) {
					progress.remove();
					Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
					return true;
				}
				progress.setText("Registration Successful");
				new RegisterActivation(act, handle).show();
			} catch (final JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			break;
		case RegisterConfirm.MSG:
			progress.setText("Sending Registration");
			final Bundle data = (Bundle) msg.obj;

			net.register(data.getString("username"), data.getString("password"), data.getString("email"));
			new Thread(net).start();
			break;
		case RegisterActivation.MSG:
			if (isTablet)
				getFragmentManager().popBackStack();
			else
				act.finish();
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

		final View view = inflater.inflate(R.layout.fragment_register, container, false);

		// create network client instance
		net = new NetworkClient(act, handle);
		progress = new ProgressMsg(act);

		// setup click listeners
		final View image = view.findViewById(R.id.register);
		image.setOnClickListener(this);

		return view;
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
		NetActive.dec(getActivity());
		super.onPause();
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() == R.id.register)
			register_validate();
	}

	private void register_validate()
	{
		EditText txt = (EditText) act.findViewById(R.id.username);
		final String username = txt.getText().toString().trim();
		if (!valid_username(username))
			return;

		txt = (EditText) act.findViewById(R.id.password);
		final String password = txt.getText().toString();
		txt = (EditText) act.findViewById(R.id.password2);
		final String password2 = txt.getText().toString();
		if (!valid_password(password, password2))
			return;

		txt = (EditText) act.findViewById(R.id.email);
		final String email = txt.getText().toString().trim();
		if (!valid_email(email))
			return;

		final Bundle bundle = new Bundle();
		bundle.putString("username", username);
		bundle.putString("password", password);
		bundle.putString("email", email);

		new RegisterConfirm(act, handle, bundle).show();
	}

	private boolean valid_username(final String name)
	{
		if (name.length() < 3) {
			Toast.makeText(act, "Username too short", Toast.LENGTH_LONG).show();
			return false;
		} else if (!name.matches("[a-zA-Z0-9]+")) {
			Toast.makeText(act, "Username can only contain letters or numbers", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	private boolean valid_password(final String pass1, final String pass2)
	{
		if (!pass1.equals(pass2)) {
			Toast.makeText(act, "Passwords don't match", Toast.LENGTH_LONG).show();
			return false;
		}
		if (pass1.length() < 4) {
			Toast.makeText(act, "Password too short", Toast.LENGTH_LONG).show();
			return false;
		}
		for (int i = 0, len = pass1.length(); i < len; i++) {
			if (pass1.charAt(i) < 32 || pass1.charAt(i) > 126) {
				Toast.makeText(act, "Password can only contain ASCII characters", Toast.LENGTH_LONG).show();
				return false;
			}
		}
		return true;
	}

	private boolean valid_email(final String email)
	{
		// regex:  \w+[\w\._+-]*\w+@\w+[\w\.-]*\w+\.\w+[\w\.-]*\w+
		if (!email.matches("\\w+[\\w\\._+-]*\\w+@\\w+[\\w\\.-]*\\w+\\.\\w+[\\w\\.-]*\\w+")) {
			Toast.makeText(act, "Invalid email address", Toast.LENGTH_LONG).show();
			return false;
		}

		final String[] part = email.split("@");
		if (part[1].contains("hotmail") || part[1].contains("live")) {
			Toast.makeText(act, "Live/Hotmail emails not supported", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
}
