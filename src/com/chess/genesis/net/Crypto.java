package com.chess.genesis;

import java.io.IOException;
import java.net.SocketException;
import java.security.MessageDigest;

final class Crypto
{
	private Crypto()
	{
	}

	private static String Sha1Hash(final String str)
	{
	try {
		final MessageDigest digst = MessageDigest.getInstance("SHA-1");

		digst.update(str.getBytes());

		final byte[] shabytes = digst.digest();
		final StringBuffer buff = new StringBuffer();
	
		for (int i = 0; i < shabytes.length; i++) {
			final String n = Integer.toHexString(shabytes[i] & 0xff);
			if (n.length() < 2)
				buff.append('0');
			buff.append(n);
		}
		return buff.toString();
	} catch (java.security.NoSuchAlgorithmException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	public static String HashPasswd(final String str)
	{
		return Sha1Hash(Sha1Hash(str));
	}

	public static String LoginKey(final String str) throws SocketException, IOException
	{
	try {
		final MessageDigest digst = MessageDigest.getInstance("SHA-1");
	
		digst.update(HashPasswd(str).getBytes());
		digst.update(SocketClient.getHash().getBytes());

		final byte[] shabytes = digst.digest();
		final StringBuffer buff = new StringBuffer();
	
		for (int i = 0; i < shabytes.length; i++) {
			final String n = Integer.toHexString(shabytes[i] & 0xff);
			if (n.length() < 2)
				buff.append('0');
			buff.append(n);
		}
		return buff.toString();
	} catch (java.security.NoSuchAlgorithmException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
