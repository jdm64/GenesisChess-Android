package com.chess.genesis;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

class PlaceLayout extends LinearLayout implements OnClickListener
{
	public static final LinearLayout.LayoutParams LINEAR_PARAMS =
		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

	private final static int[] piecelist = {1, 2, 3, 4, 5, 6, -1, -2, -3, -4, -5, -6};

	private GameState gamestate;

	public PlaceLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		setOrientation(LinearLayout.VERTICAL);

		// White Pieces
		AddPieces(context, 0);

		// Center Divide
		LinearLayout row = new LinearLayout(context);
		MyImageView padding = new MyImageView(context);
		padding.setImageResource(R.drawable.padding_480x96);
		row.addView(padding);
		addView(row);

		// Black Pieces
		AddPieces(context, 6);
	}

	private void AddPieces(final Context context, int index)
	{
		for (int i = 0; i < 2; i++) {
			final LinearLayout row = new LinearLayout(context);

			MyImageView padding = new MyImageView(context);
			padding.setImageResource((i == 0)? R.drawable.square_light : R.drawable.square_dark);
			padding.setLayoutParams(LINEAR_PARAMS);
			row.addView(padding);

			for (int j = 0; j < 3; j++) {
				final PlaceButton button = new PlaceButton(context, piecelist[index++]);
				button.setOnClickListener(this);
				row.addView(button);
			}
			padding = new MyImageView(context);
			padding.setImageResource((i == 0)? R.drawable.square_light : R.drawable.square_dark);
			padding.setLayoutParams(LINEAR_PARAMS);
			row.addView(padding);
			addView(row);
		}
	}

	public void init(final GameState _gamestate)
	{
		gamestate = _gamestate;
	}

	public void onClick(final View v)
	{
		gamestate.placeClick(v);
	}
}
