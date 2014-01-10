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

package com.chess.genesis.dialog;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;

public class BenchmarkDialog extends BaseDialog implements Handler.Callback
{
	private TextView rnps;
	private TextView gnps;

	@Override
	public boolean handleMessage(final Message msg)
	{
		final Bundle data = (Bundle) msg.obj;

		rnps.setText(String.valueOf(data.getLong(Benchmark.REG_NPS)) + " moves/sec");
		gnps.setText(String.valueOf(data.getLong(Benchmark.GEN_NPS)) + " moves/sec");

		final View button = findViewById(R.id.ok);
		button.setEnabled(true);
		return true;
	}

	public BenchmarkDialog(final Context context)
	{
		super(context);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle("CPU Benchmark");
		setBodyView(R.layout.dialog_benchmark);
		setButtonTxt(R.id.ok, "Run");
		setButtonTxt(R.id.cancel, "Close");

		rnps = (TextView) findViewById(R.id.rnps);
		gnps = (TextView) findViewById(R.id.gnps);
	}

	@Override
	public void onClick(final View v)
	{
		if (v.getId() != R.id.ok) {
			dismiss();
			return;
		}
		final View button = findViewById(R.id.ok);
		button.setEnabled(false);

		rnps.setText("running...");
		gnps.setText("running...");

		new Thread(new Benchmark(new Handler(this))).start();
	}
}
