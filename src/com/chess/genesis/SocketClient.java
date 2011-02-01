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
	private Socket sock;
	private InputStream input;
	private OutputStream output;

	public SocketClient()
	{
		sock = new Socket();
		
	}
	
	public void connect() throws SocketException, IOException
	{
		if (sock.isConnected())
			return;
		sock.connect(new InetSocketAddress("jdserver.homelinux.org", 9050));
		input = sock.getInputStream();
		output = sock.getOutputStream();
	}

	public void disconnect() throws IOException
	{
		if (!sock.isConnected())
			return;
		sock.close();
	}

	public void write(JSONObject data) throws SocketException, IOException
	{
		connect();

		String str = data.toString() + "\n";

		output.write(str.getBytes());
	}

	public JSONObject read() throws SocketException, IOException, JSONException
	{
		connect();

		byte[] buff = new byte[1000];

		input.read(buff);

		return (JSONObject) (new JSONTokener(new String(buff))).nextValue();
	}
}
