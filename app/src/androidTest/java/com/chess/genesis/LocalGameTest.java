/* GenesisChess, an Android chess application
 * Copyright 2015, Justin Madru (justin.jdm64@gmail.com)
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

package com.chess.genesis;


import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import android.view.*;
import org.hamcrest.*;
import org.junit.*;
import org.junit.runner.*;
import com.chess.genesis.R;
import com.chess.genesis.activity.*;
import com.chess.genesis.engine.*;
import com.chess.genesis.view.*;
import androidx.test.espresso.*;
import androidx.test.espresso.action.*;
import androidx.test.espresso.matcher.*;
import androidx.test.filters.*;
import androidx.test.rule.*;
import androidx.test.ext.junit.runners.*;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LocalGameTest
{
	@Rule
	public ActivityTestRule<StartActivity> rule = new ActivityTestRule<>(StartActivity.class);

	@Test
	public void promoteDialog()
	{
		onView(withId(R.id.local_game)).perform(click());
		onView(withId(R.id.new_game)).perform(click());

		onView(withId(R.id.game_type)).perform(click());
		onView(withText("Regular"))
			.inRoot(RootMatchers.isPlatformPopup())
			.perform(click());

		onView(withId(R.id.opponent)).perform(click());
		onView(withText("Human"))
			.inRoot(RootMatchers.isPlatformPopup())
			.perform(click());

		onView(withText("Create Game")).perform(click());

		onView(withId(R.id.board_layout)).perform(new BoardClicks(
		"a2a4 h7h6 a4a5 h6h5 a5a6 h5h4 a6b7 h4h3"
		));

		GeneralLocation[] locations = new GeneralLocation[]{
			GeneralLocation.TOP_LEFT, GeneralLocation.TOP_RIGHT,
			GeneralLocation.BOTTOM_LEFT, GeneralLocation.BOTTOM_RIGHT
		};
		String[] promotion = new String[]{"Q", "R", "B", "N"};

		for (int i = 0; i < 4; i++) {
			onView(withId(R.id.board_layout)).perform(new BoardClicks("b7a8"));
			onView(withId(R.id.table)).perform(doClick(locations[i]));
			onView(withId(R.id.board_layout)).check(new ValidateBoard(promotion[i] + "nbqkbnrp1ppppp32p1PPPPPPPRNBQKBNR:KQkq::9"));
			onView(withId(R.id.backwards)).perform(click());
		}
	}

	static ViewAction doClick(GeneralLocation location)
	{
		return new GeneralClickAction(
			Tap.SINGLE,
			location,
			Press.FINGER,
			InputDevice.SOURCE_UNKNOWN,
			MotionEvent.BUTTON_PRIMARY);
	}

	static class BoardClicks implements ViewAction
	{
		String moves;

		BoardClicks(String _moves)
		{
			moves = _moves;
		}

		@Override
		public Matcher<View> getConstraints()
		{
			return isDisplayed();
		}

		@Override
		public String getDescription()
		{
			return null;
		}

		@Override
		public void perform(UiController uiController, View view)
		{
			BoardView v = (BoardView) view;
			GameState state = v.getState();
			Board board = state.getBoard();
			for (String move : moves.trim().split(" +")) {
				Move mv = board.newMove();
				mv.parse(move);
				state.boardClick(v.getSquare(mv.from));
				state.boardClick(v.getSquare(mv.to));
			}
		}
	}

	static class ValidateBoard implements ViewAssertion
	{
		String zfen;

		ValidateBoard(String str)
		{
			zfen = str;
		}

		@Override
		public void check(View view, NoMatchingViewException noViewFoundException)
		{
			BoardView v = (BoardView) view;
			Assert.assertEquals(v.getState().getBoard().printZfen(), zfen);
		}
	}
}
