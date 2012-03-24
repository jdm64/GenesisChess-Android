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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

class BenchmarkDialog extends BaseDialog implements OnClickListener
{
	private TextView rnps;
	private TextView gnps;

	private final Handler handle = new Handler()
	{
		public void handleMessage(final Message msg)
		{
			final Bundle data = (Bundle) msg.obj;

			rnps.setText(String.valueOf(data.getLong("rnps")) + " moves/sec");
			gnps.setText(String.valueOf(data.getLong("gnps")) + " moves/sec");

			final View button = findViewById(R.id.ok);
			button.setEnabled(true);
		}
	};

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

	public void onClick(final View v)
	{
		if (v.getId() == R.id.ok) {
			final View button = findViewById(R.id.ok);
			button.setEnabled(false);

			rnps.setText("running...");
			gnps.setText("running...");

			(new Thread(new Benchmark(handle))).start();
		} else {
			dismiss();
		}
	}
}
