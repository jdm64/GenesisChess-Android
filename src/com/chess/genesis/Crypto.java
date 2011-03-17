package com.chess.genesis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Crypto
{
	private static String Sha1Hash(String str)
	{
		MessageDigest digst = null;
	try {
		digst = MessageDigest.getInstance("SHA-1");
	} catch (java.security.NoSuchAlgorithmException e) {
		e.printStackTrace();
	}
		digst.update(str.getBytes());

		byte[] shabytes = digst.digest();
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < shabytes.length; i++) {
			String n = Integer.toHexString(shabytes[i] & 0xff);
			if (n.length() < 2)
				buff.append('0');
			buff.append(n);
		}
		return buff.toString();
	}

	public static String HashPasswd(String str)
	{
		return Sha1Hash(Sha1Hash(str));
	}

	public static String LoginKey(String str)
	{
		MessageDigest digst = null;
	try {
		digst = MessageDigest.getInstance("SHA-1");
	} catch (java.security.NoSuchAlgorithmException e) {
		e.printStackTrace();
	}
		digst.update(HashPasswd(str).getBytes());
		digst.update(SocketClient.getHash().getBytes());

		byte[] shabytes = digst.digest();
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < shabytes.length; i++) {
			String n = Integer.toHexString(shabytes[i] & 0xff);
			if (n.length() < 2)
				buff.append('0');
			buff.append(n);
		}
		return buff.toString();
	}
}