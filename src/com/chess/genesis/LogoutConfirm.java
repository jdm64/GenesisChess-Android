package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

class LogoutConfirm extends Dialog implements OnClickListener
{
	public final static int MSG = 105;

	private Handler handle;

	public LogoutConfirm(Context context, Handler handler)
	{
		super(context);

		handle = handler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setTitle("Logout Confirmation");

		setContentView(R.layout.logout_confirm);

		Button button = (Button) findViewById(R.id.logout_ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.logout_cancel);
		button.setOnClickListener(this);
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.logout_ok:
			handle.sendMessage(handle.obtainMessage(MSG, new Bundle()));
			break;
		}
		dismiss();
	}
}
