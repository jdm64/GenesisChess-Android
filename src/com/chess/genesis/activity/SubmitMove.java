package com.chess.genesis;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;

public class SubmitMove extends Activity implements OnClickListener, OnTouchListener
{
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_submitmove);
		getWindow().setGravity(Gravity.BOTTOM);

		ImageView image = (ImageView) findViewById(R.id.submit);
		image.setOnTouchListener(this);
		image.setOnClickListener(this);

		image = (ImageView) findViewById(R.id.cancel);
		image.setOnTouchListener(this);
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

	public boolean onTouch(final View v, final MotionEvent event)
	{
		switch (v.getId()) {
		case R.id.submit:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.submit_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.submit);
			break;
		case R.id.cancel:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.cancel_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.cancel);
			break;
		}
		return false;
	}

	public void onClick(final View v)
	{
		switch (v.getId()) {
		case R.id.submit:
			setResult(RESULT_OK);
			break;
		case R.id.cancel:
			setResult(RESULT_CANCELED);
			break;
		}
		finish();
	}
}
