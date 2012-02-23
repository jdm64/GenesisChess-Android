package com.chess.genesis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;

abstract class BasePhoneActivity extends BaseActivity implements OnLongClickListener
{
	protected BaseContentFrag mainFrag;

	public void onCreate(final Bundle savedInstanceState, final BaseContentFrag Frag, final String Tag, final int layoutId)
	{
		super.onCreate(savedInstanceState);
		mainFrag = Frag;

		// set only portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set content view
		setContentView(layoutId);

		final View image = findViewById(R.id.topbar_genesis);
		image.setOnLongClickListener(this);

		final Bundle settings = (savedInstanceState != null)?
			savedInstanceState : getIntent().getExtras();

		mainFrag.setArguments(settings);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.fragment01, mainFrag, Tag).commit();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		return mainFrag.onOptionsItemSelected(item);
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
