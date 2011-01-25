package com.chess.genesis;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.util.Log;

public class GameLayout extends LinearLayout implements OnClickListener, OnTouchListener
{
	private static final int PLACE_PIECE = 1000;
	private static final int TOP_BAR = 1001;

	private ViewFlip3D board_flip;
	private TextView stm_view;

	public GameLayout(Context context)
	{
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		// Layout Params
		MyImageView button = new MyImageView(context);
		button.setImageResource(R.drawable.topbar);
		button.setOnClickListener(this);
		button.setOnTouchListener(this);
		button.setId(TOP_BAR);
		addView(button);

		// Board Content
		board_flip = new ViewFlip3D(context);
		board_flip.addView(new BoardLayout(context));
		board_flip.addView(new PlaceLayout(context));
		addView(board_flip);

		// Bottom Content
		stm_view = new TextView(context);
		stm_view.setText("White's Turn");
		addView(stm_view);

		button = new MyImageView(context);
		button.setImageResource(R.drawable.place_piece);
		button.setOnClickListener(this);
		button.setOnTouchListener(this);
		button.setId(PLACE_PIECE);
		addView(button);
	}

	public void onClick(View v)
	{
		if (v.getId() == TOP_BAR)
			return;
		board_flip.flip();
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		switch (v.getId()) {
		case PLACE_PIECE:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((MyImageView) v).setImageResource(R.drawable.place_piece_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((MyImageView) v).setImageResource(R.drawable.place_piece);
			break;
		case TOP_BAR:
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((MyImageView) v).setImageResource(R.drawable.topbar_genesis_pressed);
			else if (event.getAction() == MotionEvent.ACTION_UP)
				((MyImageView) v).setImageResource(R.drawable.topbar_genesis);
			break;
		}
		return false;
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
