package com.chess.genesis;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;

class BoardLayout extends TableLayout implements OnClickListener
{
	private GameState gamestate;

	public BoardLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		setShrinkAllColumns(true);
		setStretchAllColumns(true);

	}

	public void init(final Game game, final GameState _gamestate)
	{
		gamestate = _gamestate;

		if (game.viewAsBlack) {
			for (int i = 0; i < 8; i++) {
				final TableRow row = new TableRow(game);

				for (int j = 7; j >= 0; j--) {
					final BoardButton button = new BoardButton(game, 16 * i + j);
					button.setOnClickListener(this);
					row.addView(button);
				}
				addView(row);
			}
		} else {
			for (int i = 7; i >= 0; i--) {
				final TableRow row = new TableRow(game);

				for (int j = 0; j < 8; j++) {
					final BoardButton button = new BoardButton(game, 16 * i + j);
					button.setOnClickListener(this);
					row.addView(button);
				}
				addView(row);
			}
		}
	}

	public void onClick(final View v)
	{
		gamestate.boardClick(v);
	}
}
