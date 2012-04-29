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

package com.chess.genesis.util;

import android.net.*;
import android.os.*;
import java.io.*;
import java.util.*;

public final class FileUtils
{
	private FileUtils()
	{
	}

	private static FileInputStream tryOpenFileStream(final String path) throws FileNotFoundException
	{
		FileInputStream file = null;
		boolean error = false;
		String p = path;

		while (p.length() > 0) {
			try {
				file = new FileInputStream(p);
			} catch (final FileNotFoundException e) {
				error = true;

				final int i = p.indexOf('/', 1);
				if (i == -1)
					p = "";
				else
					p = p.substring(i);
			}
			if (!error)
				return file;
			error = false;
		}
		throw new FileNotFoundException();
	}

	public static String readFile(final String path) throws FileNotFoundException, IOException
	{
		final FileInputStream stream = tryOpenFileStream(path);
	try {
		final StringBuilder data = new StringBuilder((int) stream.getChannel().size());
		final Scanner scanner = new Scanner(stream);

		while (scanner.hasNextLine())
			data.append(scanner.nextLine());
		return data.toString();
	} finally {
		// close stream
		stream.close();
	}
	}

	public static Uri writeFile(final String filename, final String data) throws FileNotFoundException, IOException
	{
		final String state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(state))
			throw new FileNotFoundException();

		final File dir = Environment.getExternalStorageDirectory();
		final File file = new File(dir, filename);
		final FileOutputStream buffer = new FileOutputStream(file);
	try {
		buffer.write(data.getBytes());

		return Uri.fromFile(file);
	} finally {
		buffer.close();
	}
	}
}
