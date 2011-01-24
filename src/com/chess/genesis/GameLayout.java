package com.chess.genesis;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameLayout extends LinearLayout
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

		OnClickListener listen = new OnClickListener() {
			public void onClick(View v) {
				placeButtonClick();
			}
		};
		MyImageView button = new MyImageView(context);
		button.setImageResource(R.drawable.placebar);
		button.setOnClickListener(listen);
		addView(button);
	}

	public void placeButtonClick()
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

	public void setStm()
	{
		String check = " ", stm;

		stm = (GameActivity.self.board.getStm() > 0)? "White's Turn" : "Black's Turn";
		switch (GameActivity.self.board.isMate()) {
		case Board.NOT_MATE:
			if (GameActivity.self.board.incheck(GameActivity.self.board.getStm()))
				check = " (check)";
			break;
		case Board.CHECK_MATE:
			check = " (checkmate)";
			break;
		case Board.STALE_MATE:
			check = " (stalemate)";
			break;
		}
		stm_str.setText(stm + check);
	}
}
