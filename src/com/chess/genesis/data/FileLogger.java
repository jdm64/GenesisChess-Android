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

package com.chess.genesis.data;

import com.chess.genesis.util.*;
import java.io.*;

public class FileLogger
{
	private final StringBuffer buffer;
	private final Exception trace;

	public FileLogger(final Exception exception)
	{
		trace = exception;
		buffer = new StringBuffer();
	}

	public void addItem(final String name, final Object item)
	{
		buffer.append(name + "=" + String.valueOf(item) + "|");
	}

	public void write()
	{
		final String data = new PrettyDate().stdFormat() + "\n"
			+ buffer.toString() + "\n"
			+ trace.getMessage() + "\n"
			+ ObjectArray.arrayToString(trace.getStackTrace(), "\n") + "\n";

		try {
			FileUtils.writeFile("genesis-error.log", data, true);
		} catch (final FileNotFoundException e) {
			// Ignore if can't write file
		} catch (final IOException e) {
			// Ignore if can't write file
		}
	}
}
