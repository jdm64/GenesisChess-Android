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
	public static boolean isLoggedin;

	private static String loginHash;
	private static Socket sock;
	private static DataInputStream input;
	private static OutputStream output;

	// needs to be called once on program start
	public SocketClient()
	{
		disconnect();
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
		sock.connect(new InetSocketAddress("genesischess.com", 8338));
		input = new DataInputStream(sock.getInputStream());
		output = sock.getOutputStream();
		loginHash = input.readLine().trim();
	}

	public static void disconnect()
	{
	try {
		if (sock != null)
			sock.close();
		sock = new Socket();
		loginHash = null;
		isLoggedin = false;
	} catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
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
