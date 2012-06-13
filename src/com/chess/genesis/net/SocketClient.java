/*	GenesisChess, an Android chess application
	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.chess.genesis.net;

import android.content.*;
import android.net.*;
import com.chess.genesis.data.*;
import java.io.*;
import java.net.*;
import org.json.*;

public final class SocketClient
{
	private static SocketClient instance = null;

	private boolean isLoggedin;
	private String loginHash;
	private Socket socket;
	private BufferedReader input;
	private OutputStream output;

	private SocketClient()
	{
		disconnect();
	}

	public static synchronized SocketClient getInstance()
	{
		if (instance == null)
			instance = new SocketClient();
		return instance;
	}

	public static synchronized SocketClient getInstance(final int id)
	{
		return new SocketClient();
	}

	public synchronized boolean getIsLoggedIn()
	{
		return isLoggedin;
	}

	public synchronized void setIsLoggedIn(final boolean value)
	{
		isLoggedin = value;
	}

	public synchronized String getHash() throws SocketException, IOException
	{
		if (loginHash == null)
			connect();
		return loginHash;
	}

	private synchronized void connect() throws SocketException, IOException
	{
		if (socket.isConnected())
			return;
		socket.connect(new InetSocketAddress("genesischess.com", 8338));
		socket.setSoTimeout(8000);
		socket.setKeepAlive(true);
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = socket.getOutputStream();
		loginHash = input.readLine().trim();
	}

	public synchronized void disconnect()
	{
	try {
		if (socket != null)
			socket.close();
		socket = new Socket();
		loginHash = null;
		isLoggedin = false;
	} catch (final IOException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	public synchronized void write(final JSONObject data) throws SocketException, IOException
	{
		connect();

		final String str = data.toString() + "\n";

		output.write(str.getBytes());
	}

	public synchronized JSONObject read() throws SocketException, IOException, JSONException
	{
		connect();

	try {
		return (JSONObject) new JSONTokener(input.readLine()).nextValue();
	} catch (final NullPointerException e) {
		return new JSONObject("{\"result\":\"error\",\"reason\":\"connection lost\"}");
	}
	}

	public void logError(final Context context, final Exception trace, final JSONObject json)
	{
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		final FileLogger logger = new FileLogger(trace);

		logger.addItem("NetActive", (netInfo != null)? netInfo.isConnected() : null);
		logger.addItem("isConnected", socket.isConnected());
		logger.addItem("isLoggedIn", isLoggedin);
		logger.addItem("loginHash", loginHash);
		logger.addItem("request", json.toString());
		logger.write();
	}
}
