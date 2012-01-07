package com.chess.genesis;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class SubmitMove extends Activity implements OnClickListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_submitmove);
		getWindow().setGravity(Gravity.BOTTOM);

		ImageView image = (ImageView) findViewById(R.id.submit);
		image.setOnClickListener(this);
		image = (ImageView) findViewById(R.id.cancel);
		image.setOnClickListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		NetActive.inc();
	}

	@Override
	public void onPause()
	{
		NetActive.dec();
		super.onPause();
	}

	@Override
	public void onBackPressed()
	{
		setResult(RESULT_CANCELED);
	}

	public void onClick(final View v)
	{
		if (v.getId() == R.id.submit)
			setResult(RESULT_OK);
		else if (v.getId() == R.id.cancel)
			setResult(RESULT_CANCELED);
		finish();
	}
}
