package com.chess.genesis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnLongClickListener;

class BasePhoneActivity extends FragmentActivity implements OnLongClickListener
{
	public void onCreate(final Bundle savedInstanceState, final int layoutId)
	{
		super.onCreate(savedInstanceState);

		// set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(layoutId);

		final View image = findViewById(R.id.topbar_genesis);
		image.setOnLongClickListener(this);
	}

	public boolean onLongClick(final View v)
	{
		if (v.getId() == R.id.topbar_genesis) {
			finish();
			return true;
		}
		return false;
	}
}
