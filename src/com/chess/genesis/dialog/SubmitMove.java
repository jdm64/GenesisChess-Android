package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class SubmitMove extends Dialog implements OnClickListener
{
	public final static int MSG = 122;

	private final Handler handle;
	private final boolean isTablet;

	public SubmitMove(final Context context, final Handler handler, final boolean TabletMode)
	{
		super(context, R.style.BlankDialog);

		handle = handler;
		isTablet = TabletMode;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (isTablet)
			setContentView(R.layout.dialog_submitmove_tablet);
		else
			setContentView(R.layout.dialog_submitmove_phone);
		getWindow().setGravity(Gravity.BOTTOM);

		View image = findViewById(R.id.submit);
		image.setOnClickListener(this);
		image = findViewById(R.id.cancel);
		image.setOnClickListener(this);
	}

	@Override
	public void onBackPressed()
	{
		handle.sendMessage(handle.obtainMessage(MSG, false));
		dismiss();
	}

	public void onClick(final View v)
	{
		handle.sendMessage(handle.obtainMessage(MSG, v.getId() == R.id.submit));
		dismiss();
	}
}
