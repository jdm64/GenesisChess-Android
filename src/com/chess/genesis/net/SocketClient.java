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

package com.chess.genesis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

final class SocketClient
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
		socket.setSoTimeout(5000);
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
	} catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException();
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
		return (JSONObject) (new JSONTokener(input.readLine())).nextValue();
	} catch (NullPointerException e) {
		return new JSONObject("{\"result\":\"error\",\"reason\":\"connection lost\"}");
	}
	}
}
