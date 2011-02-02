package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainMenu extends Activity implements OnClickListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// set content view
		setContentView(R.layout.mainmenu);

		// setup click listeners
		Button button = (Button) findViewById(R.id.local_game);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.register);
		button.setOnClickListener(this);
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.local_game:
			startActivity(new Intent(this, GameList.class));
			break;
		case R.id.register:
			startActivity(new Intent(this, Register.class));
			break;
		}
	}
}
