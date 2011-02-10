package com.chess.genesis;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

class PlaceLayout extends LinearLayout implements OnClickListener
{
	public static final LinearLayout.LayoutParams linearparams =
		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

	public PlaceLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		int[] piecelist = {1, 2, 3, 4, 5, 6, -1, -2, -3, -4, -5, -6};
		int count = 0;

		setOrientation(LinearLayout.VERTICAL);

		LinearLayout row;
		MyImageView padding;
		PlaceButton button;

		// White Pieces
		for (int i = 0; i < 2; i++) {
			row = new LinearLayout(context);

			padding = new MyImageView(context);
			padding.setImageResource(R.drawable.padding_96x96);
			padding.setLayoutParams(linearparams);
			row.addView(padding);

			for (int j = 0; j < 3; j++) {
				button = new PlaceButton(context, piecelist[count++]);
				button.setOnClickListener(this);
				row.addView(button);
			}
			padding = new MyImageView(context);
			padding.setImageResource(R.drawable.padding_96x96);
			padding.setLayoutParams(linearparams);
			row.addView(padding);
			addView(row);
		}

		// Center Divide
		row = new LinearLayout(context);
		padding = new MyImageView(context);
		padding.setImageResource(R.drawable.padding_480x96);
		row.addView(padding);
		addView(row);

		// Black Pieces
		for (int i = 0; i < 2; i++) {
			row = new LinearLayout(context);

			padding = new MyImageView(context);
			padding.setImageResource(R.drawable.padding_96x96);
			padding.setLayoutParams(linearparams);
			row.addView(padding);

			for (int j = 0; j < 3; j++) {
				button = new PlaceButton(context, piecelist[count++]);
				button.setOnClickListener(this);
				row.addView(button);
			}
			padding = new MyImageView(context);
			padding.setImageResource(R.drawable.padding_96x96);
			padding.setLayoutParams(linearparams);
			row.addView(padding);
			addView(row);
		}
	}

	public void onClick(View v)
	{
		GameState.self.placeClick(v);
	}
}