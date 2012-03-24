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

import java.io.IOException;
import java.net.SocketException;
import java.security.MessageDigest;

final class Crypto
{
	private Crypto()
	{
	}

	private static String calcHash(final MessageDigest digst)
	{
		final byte[] shabytes = digst.digest();
		final StringBuffer buff = new StringBuffer();
	
		for (int i = 0; i < shabytes.length; i++) {
			final String n = Integer.toHexString(shabytes[i] & 0xff);
			if (n.length() < 2)
				buff.append('0');
			buff.append(n);
		}
		return buff.toString();
	}

	private static String Sha1Hash(final String str)
	{
	try {
		final MessageDigest digst = MessageDigest.getInstance("SHA-1");

		digst.update(str.getBytes());
		return calcHash(digst);
	} catch (java.security.NoSuchAlgorithmException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}

	public static String HashPasswd(final String str)
	{
		return Sha1Hash(Sha1Hash(str));
	}

	public static String LoginKey(final SocketClient socket, final String str) throws SocketException, IOException
	{
	try {
		final MessageDigest digst = MessageDigest.getInstance("SHA-1");
	
		digst.update(HashPasswd(str).getBytes());
		digst.update(socket.getHash().getBytes());

		return calcHash(digst);
	} catch (java.security.NoSuchAlgorithmException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
