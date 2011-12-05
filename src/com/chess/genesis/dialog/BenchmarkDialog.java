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
