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

package com.chess.genesis.net;

import android.content.*;
import android.net.*;

import com.chess.genesis.*;
import com.chess.genesis.data.*;
import java.io.*;
import java.net.*;
import org.json.*;

public final class SocketClient
{
	private static SocketClient instance = null;
	private static String server = null;
	private static Boolean debug = null;
	private static final int port = 8338;

	private boolean isLoggedin;
	private String loginHash;
	private Socket socket;
	private BufferedReader input;
	private OutputStream output;

	private SocketClient(final Context ctx)
	{
		disconnect();
		if (server == null)
			initHost(ctx);
		if (debug == null)
			initDebug(ctx);
	}

	public static void initHost(final Context ctx)
	{
		final PrefEdit pref = new PrefEdit(ctx);
		server = pref.getString(R.array.pf_serverhost);
		if (server.isEmpty()) {
			pref.putString(R.array.pf_serverhost);
			pref.commit();
			server = pref.getString(R.array.pf_serverhost);
		}
	}

	public static void initDebug(final Context ctx)
	{
		final PrefEdit pref = new PrefEdit(ctx);
		debug = pref.getBool(R.array.pf_netdebug);
	}

	public static synchronized SocketClient getInstance(final Context ctx)
	{
		if (instance == null)
			instance = new SocketClient(ctx);
		return instance;
	}

	public static synchronized SocketClient getNewInstance(final Context ctx)
	{
		return new SocketClient(ctx);
	}

	public synchronized boolean getIsLoggedIn()
	{
		return isLoggedin;
	}

	public synchronized void setIsLoggedIn(final boolean value)
	{
		isLoggedin = value;
	}

	public synchronized String getHash() throws IOException
	{
		if (loginHash == null)
			connect();
		return loginHash;
	}

	private synchronized void connect() throws IOException
	{
		if (socket.isConnected())
			return;
		socket.connect(new InetSocketAddress(server, port));
		socket.setSoTimeout(8000);
		socket.setKeepAlive(true);
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = socket.getOutputStream();
		loginHash = input.readLine().trim();
		logTraffic(loginHash, true);
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

	public synchronized void write(final JSONObject data) throws IOException
	{
		connect();

		String dataStr = data.toString();
		logTraffic(dataStr, false);

		output.write(new String(dataStr + '\n').getBytes());
	}

	public synchronized JSONObject read() throws IOException, JSONException
	{
		connect();

	try {
		String data = input.readLine();
		logTraffic(data, true);

		return (JSONObject) new JSONTokener(data).nextValue();
	} catch (final NullPointerException e) {
		return new JSONObject("{\"result\":\"error\",\"reason\":\"connection lost\"}");
	}
	}

	private void logTraffic(String data, boolean isRead)
	{
		if (debug) {
			String dirStr = isRead? "<-- " : "--> ";
			new FileLogger(null).addData(dirStr + data).write();
		}
	}

	public void logError(final Context context, final Exception trace, final JSONObject json)
	{
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		final FileLogger logger = new FileLogger(trace);

		logger.addItem("NetActive", (netInfo != null)? netInfo.isConnected() : null)
			.addItem("isConnected", socket.isConnected())
			.addItem("isLoggedIn", isLoggedin)
			.addItem("loginHash", loginHash)
			.addItem("request", json.toString())
			.write();
	}
}
