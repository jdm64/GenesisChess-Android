package com.chess.genesis;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;

class BoardLayout extends TableLayout implements OnClickListener
{
	public BoardLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setShrinkAllColumns(true);
		setStretchAllColumns(true);

		for (int i = 0; i < 8; i++) {
			TableRow row = new TableRow(context);
						
			for (int j = 0; j < 8; j++) {
				BoardButton button = new BoardButton(context, i * 8 + j);
				button.setOnClickListener(this);
				row.addView(button);
			}
			addView(row);
		}
	}

	public void onClick(View v)
	{
		GameState.self.boardClick(v);
	}
}
