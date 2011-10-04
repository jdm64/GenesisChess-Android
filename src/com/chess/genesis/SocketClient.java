package com.chess.genesis;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

class SocketClient
{
	public static boolean isLoggedin = false;

	private static String loginHash = null;
	private static Socket sock = new Socket();
	private static DataInputStream input;
	private static OutputStream output;

	public SocketClient()
	{
	}

	public static String getHash() throws SocketException, IOException
	{
		if (loginHash == null)
			connect();
		return loginHash;
	}

	private static void connect() throws SocketException, IOException
	{
		if (sock.isConnected())
			return;
		hard_disconnect();
		sock.connect(new InetSocketAddress("genesischess.com", 8338));
		input = new DataInputStream(sock.getInputStream());
		output = sock.getOutputStream();

		loginHash = input.readLine().trim();
	}

	public static void hard_disconnect()
	{
	try {
		sock.close();
		sock = new Socket();
		loginHash = null;
		isLoggedin = false;
	} catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	public static void disconnect()
	{
	}

	public static void write(final JSONObject data) throws SocketException, IOException
	{
		connect();

		final String str = data.toString() + "\n";

		output.write(str.getBytes());
	}

	public static JSONObject read() throws SocketException, IOException, JSONException
	{
		connect();

		return (JSONObject) (new JSONTokener(input.readLine())).nextValue();
	}
}
