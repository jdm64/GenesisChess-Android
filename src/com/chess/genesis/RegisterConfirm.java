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
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

class RegisterConfirm extends Dialog implements OnClickListener
{
	public final static int MSG = 106;

	private Handler handle;
	private Bundle data;

	public RegisterConfirm(Context context, Handler handler, Bundle bundle)
	{
		super(context);

		handle = handler;
		data = bundle;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setTitle("Register Confirmation");

		setContentView(R.layout.register_confirm);

		Button button = (Button) findViewById(R.id.register_ok);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.register_cancel);
		button.setOnClickListener(this);

		TextView text = (TextView) findViewById(R.id.username);
		text.setText(data.getString("username"));

		text = (TextView) findViewById(R.id.email);
		text.setText(data.getString("email"));
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.register_ok:
			handle.sendMessage(handle.obtainMessage(MSG, data));
			break;
		}
		dismiss();
	}
}
