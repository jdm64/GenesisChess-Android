package com.chess.genesis;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameLayout extends LinearLayout implements OnClickListener
{
	private ViewFlip3D board_flip;
	private TextView stm_view;

	public GameLayout(Context context)
	{
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		// Layout Params
		MyImageView titlebar = new MyImageView(context);
		titlebar.setImageResource(R.drawable.titlebar);
		addView(titlebar);

		// Board Content
		board_flip = new ViewFlip3D(context);
		board_flip.addView(new BoardLayout(context));
		board_flip.addView(new PlaceLayout(context));
		addView(board_flip);

		// Bottom Content
		stm_view = new TextView(context);
		stm_view.setText("White's Turn");
		addView(stm_view);

		MyImageView button = new MyImageView(context);
		button.setImageResource(R.drawable.placebar);
		button.setOnClickListener(this);
		addView(button);
	}

	public void onClick(View v)
	{
		board_flip.flip();
	}

	public void resetPieces()
	{
		for (int i = 0; i < 64; i++) {
			BoardButton square = (BoardButton) findViewById(i);
			square.resetSquare();
		}
		for (int i = 94; i < 100; i++) {
			PlaceButton piece = (PlaceButton) findViewById(i);
			piece.reset();
		}
		for (int i = 101; i < 107; i++) {
			PlaceButton piece = (PlaceButton) findViewById(i);
			piece.reset();
		}
	}

	public void setStm(String stm)
	{
		stm_view.setText(stm);
	}
}
