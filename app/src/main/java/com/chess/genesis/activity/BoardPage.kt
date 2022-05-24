/* GenesisChess, an Android chess application
 * Copyright 2022, Justin Madru (justin.jdm64@gmail.com)
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
package com.chess.genesis.activity

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.chess.genesis.api.IGameController2
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GamePage(nav: NavHostController) {
	var state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var gameCtlr = GameController.get(LocalContext.current)

	ModalBottomSheetLayout(
		sheetElevation = 16.dp,
		sheetShape = RoundedCornerShape(32.dp),
		sheetState = state,
		sheetContent = { BottomBarMenu(state, nav) })
	{
		GameContent(gameCtlr, state)
	}

	showGameDialogs(gameCtlr)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomBarMenu(state: ModalBottomSheetState, nav: NavHostController) {
	var ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	Column {
		ListItem(
			modifier = Modifier.clickable(onClick = { scope.launch { state.hide() } }),
			icon = { Icon(Icons.Filled.Home, "Game Board") },
			text = { Text("Game Board") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = { nav.navigate("list") }),
			icon = { Icon(Icons.Filled.List, "Game List") },
			text = { Text("Game List") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				scope.launch {
					ctx.startActivity(Intent(ctx, SettingsPage::class.java))
					state.hide()
				}
			}),
			icon = { Icon(Icons.Filled.Settings, "Settings") },
			text = { Text("Settings") }
		)
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GameContent(gameCtlr: IGameController2, state: ModalBottomSheetState) {
	var showCapture = remember { gameCtlr.showCapture() }

	Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
		TopAppBar {
			Text("GENESIS CHESS")
		}
		Column {
			AndroidView({ gameCtlr.boardView })
			if (showCapture.value) {
				AndroidView({ gameCtlr.capturedView })
			}
		}
		BottomBar(state) { GameNav(gameCtlr) }
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomBar(state: ModalBottomSheetState, content: @Composable () -> Unit) {
	val scope = rememberCoroutineScope()

	BottomAppBar(Modifier.height(60.dp)) {
		IconButton(onClick = { scope.launch { state.show() } }) {
			Icon(Icons.Filled.Menu, "menu", Modifier.size(30.dp))
		}
		Spacer(Modifier.aspectRatio(1.0f))
		content.invoke()
	}
}

@Composable
fun GameNav(gameCtlr: IGameController2) {
	val isGen = remember { gameCtlr.isGenChess }

	IconButton(onClick = { gameCtlr.onBackClick() }) {
		Icon(Icons.Filled.ArrowBack, "Back", Modifier.size(30.dp))
	}
	IconButton(onClick = { gameCtlr.onForwardClick() }) {
		Icon(Icons.Filled.ArrowForward, "Forward", Modifier.size(30.dp))
	}
	IconButton(onClick = { gameCtlr.onCurrentClick() }) {
		Icon(Icons.Filled.PlayArrow, "Last", Modifier.size(30.dp))
	}
	if (isGen.value) {
		IconButton(onClick = { gameCtlr.onPlaceClick() }) {
			Icon(Icons.Filled.Place, "Place", Modifier.size(30.dp))
		}
	}
}

@Composable
fun showGameDialogs(gameCtlr: IGameController2) {
	val promoteState = remember { gameCtlr.promoteState }
	if (promoteState.value) {
		AlertDialog(onDismissRequest = { promoteState.value = false },
			title = {
				Text(
					text = "Promote Pawn",
					fontWeight = FontWeight.Bold,
					fontSize = 20.sp
				)
			},
			text = { AndroidView({ gameCtlr.promoteView }) },
			buttons = {
				TextButton(onClick = { promoteState.value = false }) {
					Text("Cancel")
				}
			}
		)
	}
	val placeState = remember { gameCtlr.placeState }
	if (placeState.value) {
		AlertDialog(onDismissRequest = { placeState.value = false },
			title = {
				Text(
					text = "Place Piece",
					fontWeight = FontWeight.Bold,
					fontSize = 20.sp
				)
			},
			text = { AndroidView({ gameCtlr.placeView }) },
			buttons = {
				TextButton(onClick = { placeState.value = false }) {
					Text("Cancel")
				}
			}
		)
	}
}
