package com.chess.genesis;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

class ProgressMsg extends ProgressDialog implements Runnable
{
	public final static int MSG = 121;

	private Handler handle = null;

	public ProgressMsg(final Context context)
	{
		super(context);
	}

	public ProgressMsg(final Context context, final Handler handler)
	{
		super(context);
		handle = handler;
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (handle != null)
			handle.sendMessage(handle.obtainMessage(MSG));
	}

	public void onBackPressed()
	{
	}

	public void remove()
	{
		(new Thread(this)).start();
	}

	public void setText(final String msg)
	{
		setMessage(msg);
		if (!isShowing())
			show();
	}

	public void run()
	{
	try {
		Thread.sleep(256);
		dismiss();
	} catch (InterruptedException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
