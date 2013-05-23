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

import java.io.*;
import java.nio.*;
import java.security.*;

public final class Crypto
{
	private Crypto()
	{
	}

	private static String calcHash(final MessageDigest digst)
	{
		final byte[] shabytes = digst.digest();
		final StringBuilder buff = new StringBuilder();

		for (final byte shabyte : shabytes) {
			final String n = Integer.toHexString(shabyte & 0xff);
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
	} catch (final NoSuchAlgorithmException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	public static String RandomSha1Hash()
	{
	try {
		final MessageDigest digst = MessageDigest.getInstance("SHA-1");
		final SecureRandom random = new SecureRandom();
		final byte[] buffer = new byte[512];
		final long time = System.nanoTime();

		random.nextBytes(buffer);
		digst.update(buffer);
		digst.update(ByteBuffer.allocate(8).putLong(time).array());

		return calcHash(digst);
	} catch (final NoSuchAlgorithmException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}

	public static String HashPasswd(final String str)
	{
		return Sha1Hash(Sha1Hash(str));
	}

	public static String LoginKey(final SocketClient socket, final String str) throws IOException
	{
	try {
		final MessageDigest digst = MessageDigest.getInstance("SHA-1");

		digst.update(HashPasswd(str).getBytes());
		digst.update(socket.getHash().getBytes());

		return calcHash(digst);
	} catch (final NoSuchAlgorithmException e) {
		throw new RuntimeException(e.getMessage(), e);
	}
	}
}
