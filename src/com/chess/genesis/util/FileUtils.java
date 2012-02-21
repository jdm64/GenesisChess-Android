package com.chess.genesis;

import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

final class FileUtils
{
	private FileUtils()
	{
	}

	private static FileInputStream tryOpenFileStream(final String path) throws FileNotFoundException
	{
		FileInputStream file = null;
		boolean error = false;
		String p = new String(path);

		while (p.length() > 0) {
			try {
				file = new FileInputStream(p);
			} catch (FileNotFoundException e) {
				error = true;

				final int i = p.indexOf('/', 1);
				if (i == -1)
					p = new String();
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

		buffer.write(data.getBytes());
		buffer.close();

		return Uri.fromFile(file);
	}
}
