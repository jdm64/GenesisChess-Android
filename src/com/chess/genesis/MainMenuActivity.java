package com.chess.genesis;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class MainMenuActivity extends Activity implements OnClickListener
{
	public static final int LOCAL_GAME = 1;
	public static final int ONLINE_GAME = 2;
	public static final int SETTINGS = 3;
	
	public static MainMenuActivity self;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		self = this;

		// Set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// set content view
		MainMenuLayout view = new MainMenuLayout(this);
		setContentView(view);
	}

	@Override
	public void onBackPressed()
	{
		moveTaskToBack(true);		
	}

	public void onClick(View v)
	{
		switch (v.getId()) {
		case LOCAL_GAME:
			startActivity(new Intent(this, GameActivity.class));
			break;
		}
	}
}
