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
import android.os.Handler.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import org.json.*;
import com.chess.genesis.*;
import com.chess.genesis.dialog.*;
import com.chess.genesis.net.*;

public class RegisterFrag extends AbstractActivityFrag implements Callback, OnClickListener
{
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
					progress.dismiss();
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
			progress.dismiss();

			act.finish();

			final Intent intent = new Intent("register");
			intent.putExtras(getRegisterData());
			BroadcastWrapper.send(act, intent);
			break;
		}
		return true;
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
		final View view = inflater.inflate(R.layout.fragment_register, container, false);

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
		final Bundle bundle = getRegisterData();

		if (!valid_username(bundle.getString("username")))
			return;

		if (!valid_password(bundle.getString("password"), bundle.getString("password2")))
			return;

		if (!valid_email(bundle.getString("email")))
			return;

		RegisterConfirm.create(handle, bundle).show(getFragmentManager(), "");
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

	private Bundle getRegisterData()
	{
		final Bundle bundle = new Bundle();

		EditText txt = act.findViewById(R.id.username);
		bundle.putString("username", txt.getText().toString().trim());

		txt = act.findViewById(R.id.password);
		bundle.putString("password", txt.getText().toString());

		txt = act.findViewById(R.id.password2);
		bundle.putString("password2", txt.getText().toString());

		txt = act.findViewById(R.id.email);
		bundle.putString("email", txt.getText().toString().trim());

		return bundle;
	}
}
