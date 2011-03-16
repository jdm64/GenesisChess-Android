package com.chess.genesis;

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
	private static String loginHash = null;
	private static Socket sock = new Socket();

	private static InputStream input;
	private static OutputStream output;

	public SocketClient()
	{
		NetActive.inc();
	}

	public static String getHash()
	{
	try {
		if (loginHash == null)
			connect();
	} catch (SocketException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
		return loginHash;
	}

	private static void connect() throws SocketException, IOException
	{
		if (sock.isConnected())
			return;
		sock.connect(new InetSocketAddress("jdserver.homelinux.org", 8338));
		input = sock.getInputStream();
		output = sock.getOutputStream();

		byte[] buff = new byte[1440];
		input.read(buff);
		loginHash = (new String(buff)).trim();
	}

	public static void hard_disconnect()
	{
	try {
		if (!sock.isConnected())
			return;
		sock.close();
		sock = new Socket();
		loginHash = null;
	} catch (IOException e) {
		throw new RuntimeException();
	}
	}

	public static void disconnect() throws IOException
	{
		NetActive.dec();
	}

	public static void write(JSONObject data) throws SocketException, IOException
	{
		connect();

		String str = data.toString() + "\n";

		output.write(str.getBytes());
	}

	public static JSONObject read() throws SocketException, IOException, JSONException
	{
		connect();

		byte[] buff = new byte[1440];

		input.read(buff);

		return (JSONObject) (new JSONTokener(new String(buff))).nextValue();
	}
}
