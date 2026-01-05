/* GenesisChess, an Android chess application
 * Copyright 2026, Justin Madru (justin.jdm64@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chess.genesis.view;

import java.util.*;
import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.os.*;
import android.view.*;
import com.chess.genesis.api.*;
import com.chess.genesis.data.Enums.*;
import com.chess.genesis.engine.*;

public class StmView extends View
{
	private StmState stmState;
	private ClockState clockState;

	private final Paint painter = new Paint();
	private final Rect whiteBox = new Rect();
	private final Rect blackBox = new Rect();

	private int viewHeight;

	private final Handler timerHandler = new Handler(Looper.getMainLooper());
	private final Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			if (clockState != null && clockState.type() != ClockType.NO_CLOCK) {
				invalidate();
			}
			timerHandler.postDelayed(this, 1000);
		}
	};

	public StmView(Context context)
	{
		super(context);

		PieceImgPainter.initColors(context);
		stmState = new StmState("White", "Black", 0, GameStatus.ACTIVE, 0);
		clockState = new ClockState(ClockType.NO_CLOCK, -1, 0, 0, 0);
	}

	public void setStmState(StmState state)
	{
		stmState = state;
		invalidate();
	}

	public void setClockState(ClockState clock)
	{
		clockState = clock;
		invalidate();
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		timerHandler.postDelayed(timerRunnable, 1000);
	}

	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		timerHandler.removeCallbacks(timerRunnable);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		var width = MeasureSpec.getSize(widthMeasureSpec);
		viewHeight = width / 6;
		var height = viewHeight;
		var halfWidth = width / 2;

		whiteBox.set(0, 0, halfWidth, height);
		blackBox.set(halfWidth, 0, width, height);

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// Check if we have valid state data
		if (stmState == null || clockState == null) {
			return;
		}

		drawPlayerSection(true, whiteBox, canvas);
		drawPlayerSection(false, blackBox, canvas);
	}

	private void drawPlayerSection(boolean isWhite, Rect rect, Canvas canvas)
	{
		var sideColor = isWhite ? Piece.WHITE : Piece.BLACK;
		var isStm = stmState.stm() == sideColor;
		var playerName = isWhite ? stmState.white() : stmState.black();
		var playerTime = isWhite ? clockState.whiteTime() : clockState.blackTime();

		var timeRemaining = playerTime;
		if (sideColor == clockState.stm() && clockState.type() != ClockType.NO_CLOCK && clockState.lastMove() > 0) {
			var currentTime = System.currentTimeMillis();
			var timeElapsed = currentTime - clockState.lastMove();
			timeRemaining = playerTime - timeElapsed;
		}

		var timeStr = formatTime(timeRemaining);

		var left = rect.left;
		var top = rect.top;
		var right = rect.right;
		var bottom = rect.bottom;
		var centerX = (left + right) / 2;

		// Draw stm indicator

		if (timeRemaining < 0) {
			painter.setColor(PieceImgPainter.innerCheck);
			canvas.drawRect(left, top, right, bottom, painter);
		} else if (isStm) {
			var stmColor = switch (stmState.status()) {
				case WAITING_FOR_OPPONENT, ACTIVE -> isWhite ? PieceImgPainter.outerDark : PieceImgPainter.outerLight;
				case WHITE_MATE, BLACK_MATE -> PieceImgPainter.innerCheck;
				case STALEMATE -> PieceImgPainter.innerLast;
			};
			painter.setColor(stmColor);
			canvas.drawRect(left, top, right, bottom, painter);
		}

		// Draw background
		painter.setColor(isWhite ? PieceImgPainter.innerLight : PieceImgPainter.innerDark);
		var margin = 0.06f * viewHeight;
		canvas.drawRect(left + margin, top + margin, right - margin, bottom - margin, painter);

		// Draw your color indicator
		if (sideColor == stmState.yourColor()) {
			painter.setColor(PieceImgPainter.innerSelect);
			canvas.drawCircle(left + 0.25f * viewHeight, 0.75f * viewHeight, viewHeight / 10.0f, painter);
		}

		// Draw player name
		painter.setTextSize(viewHeight / 3.0f);
		painter.setColor(isWhite ? PieceImgPainter.outerDark : PieceImgPainter.innerLight);
		painter.setTextAlign(Align.LEFT);
		canvas.drawText(playerName, left + 0.15f * viewHeight, 0.40f * viewHeight, painter);

		// Draw time
		painter.setTextAlign(Align.CENTER);
		canvas.drawText(timeStr, centerX, 0.85f * viewHeight, painter);
	}

	private String formatTime(long ms)
	{
		if (ms <= 0) {
			return "00m : 00s";
		}

		// If time is under 10 seconds, format as X.YYYs
		if (ms < 10000) {
			var seconds = ms / 1000.0;
			return String.format(Locale.getDefault(), "%.3fs", seconds);
		}

		var totalSeconds = ms / 1000;

		// If time is 1 hour or over, format as Xh : YYm
		if (totalSeconds >= 3600) {
			long hours = totalSeconds / 3600;
			long minutes = (totalSeconds % 3600) / 60;
			return String.format(Locale.getDefault(), "%02dh : %02dm", hours, minutes);
		}

		// If time is under 1 hour, format as XXm : YYs
		long minutes = totalSeconds / 60;
		long seconds = totalSeconds % 60;
		return String.format(Locale.getDefault(), "%02dm : %02ds", minutes, seconds);
	}
}
