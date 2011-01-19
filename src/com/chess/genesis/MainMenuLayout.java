package com.chess.genesis;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainMenuLayout extends LinearLayout
{
	public MainMenuLayout(Context context)
	{
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		Button button = new Button(context);
		button.setText("Local Games");
		button.setId(MainMenuActivity.LOCAL_GAME);
		button.setOnClickListener(MainMenuActivity.self);
		addView(button);

		button = new Button(context);
		button.setText("Online Games");
		addView(button);

		button = new Button(context);
		button.setText("Settings");
		addView(button);
	}
}
