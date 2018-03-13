/* GenesisChess, an Android chess application
 * Copyright 2014, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.genesis.dialog;

import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.chess.genesis.*;
import com.chess.genesis.engine.*;
import java.util.Map.*;

public class BenchmarkDialog extends DialogFragment implements DialogInterface.OnClickListener, Handler.Callback, View.OnClickListener
{
	private TextView rnps;
	private TextView gnps;

	public static BenchmarkDialog create()
	{
		return new BenchmarkDialog();
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog == null) {
			return true;
		}
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);

		Bundle data = (Bundle) msg.obj;
		rnps.setText(data.getLong(Benchmark.REG_NPS) + " moves/sec");
		gnps.setText(data.getLong(Benchmark.GEN_NPS) + " moves/sec");

		return true;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		Entry<View, Builder> builder = DialogUtil.createViewBuilder(this, R.layout.dialog_benchmark);

		builder.getValue()
			.setTitle("CPU Benchmark")
			.setPositiveButton("Run", this)
			.setNegativeButton("Close", this);

		rnps = builder.getKey().findViewById(R.id.rnps);
		gnps = builder.getKey().findViewById(R.id.gnps);

		return builder.getValue().create();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		AlertDialog dialog = (AlertDialog) getDialog();
		Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View view)
	{
		AlertDialog dialog = (AlertDialog) getDialog();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

		rnps.setText("running...");
		gnps.setText("running...");

		new Thread(new Benchmark(new Handler(this))).start();
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		dismiss();
	}
}
