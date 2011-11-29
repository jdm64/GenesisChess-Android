package com.chess.genesis;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

abstract class BaseDialog extends Dialog implements OnClickListener
{
	public final static int CANCEL = 1;
	public final static int OKCANCEL = 3;

	private final int buttonCount;

	public BaseDialog(final Context context)
	{
		super(context);
		buttonCount = OKCANCEL;
	}

	public BaseDialog(final Context context, final int ButtonCount)
	{
		super(context);
		buttonCount = ButtonCount;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		Button button;

		if (buttonCount == OKCANCEL) {
			setContentView(R.layout.dialog_base_okcancel);

			button = (Button) findViewById(R.id.ok);
			button.setOnClickListener(this);
		} else {
			setContentView(R.layout.dialog_base_cancel);
		}
		button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(this);
	}

	public void setBodyView(final int layoutID)
	{
		final ViewGroup gView = (ViewGroup) findViewById(R.id.body);
		getLayoutInflater().inflate(layoutID, gView, true);
	}

	public void setButtonTxt(final int buttonId, final String txt)
	{
		final Button button = (Button) findViewById(buttonId);

		if (button == null)
			return;
		button.setText(txt);
	}
}
