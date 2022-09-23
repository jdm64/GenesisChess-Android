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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.navigation.NavHostController
import com.chess.genesis.R
import com.chess.genesis.api.IGameController
import com.chess.genesis.api.SubmitState
import com.chess.genesis.controller.GameController
import com.chess.genesis.data.Pref
import com.chess.genesis.engine.Piece
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GamePage(nav: NavHostController, gameId: String) {
	val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	val ctx = LocalContext.current
	val gameCtlr = remember { GameController(ctx, gameId) }

	DisposableEffect(gameCtlr) {
		onDispose { gameCtlr.onDispose()  }
	}

	ModalBottomSheetLayout(
		sheetElevation = 16.dp,
		sheetShape = RoundedCornerShape(32.dp),
		sheetState = state,
		sheetContent = { GameMenu(gameCtlr, state, nav) })
	{
		GameContent(gameCtlr, state)
	}

	ShowGameDialogs(gameCtlr)

	ShowSubmitDialog(gameCtlr)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GameMenu(gameCtlr: GameController, state: ModalBottomSheetState, nav: NavHostController) {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	Column {
		ListItem(
			modifier = Modifier.clickable(onClick = {
				val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				val clip = ClipData.newPlainText("simple text", gameCtlr.gameId)
				clipboard.setPrimaryClip(clip)
				Toast.makeText(ctx, "Game ID copied", Toast.LENGTH_SHORT).show()
				scope.launch { state.hide() }
			}),
			icon = { Icon(Icons.Filled.Share, "Copy Game ID") },
			text = { Text("Copy Game ID") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				if (!nav.popBackStack("list", false)) {
					nav.navigate("list")
				}
				scope.launch { state.hide() }
			}),
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
fun GameContent(gameCtlr: IGameController, state: ModalBottomSheetState) {
	val stmState = remember { gameCtlr.stmState }
	val colors = MaterialTheme.colors
	val whiteColor = if (stmState.value.mate * stmState.value.stm > 0)
		colors.error else colors.onPrimary
	val blackColor = if (stmState.value.mate * stmState.value.stm < 0)
		colors.error else colors.onPrimary

	Column(Modifier.fillMaxHeight().background(Color.Gray), verticalArrangement = Arrangement.SpaceBetween) {
		TopAppBar {
			Column {
				val id = gameCtlr.gameId
				Text("Game ID: $id")
				TabRow(selectedTabIndex = if (stmState.value.stm == Piece.WHITE) 0 else 1) {
					Tab(
						onClick = {},
						selected = stmState.value.stm == Piece.WHITE,
						text = {
							Text(
								stmState.value.white,
								color = whiteColor
							)
						})
					Tab(
						onClick = {},
						selected = stmState.value.stm == Piece.BLACK,
						text = {
							Text(
								stmState.value.black,
								color = blackColor
							)
						})
				}
			}
		}
		BoardAndPieces(gameCtlr)
		BottomBar(state) { GameNav(gameCtlr) }
	}
}

@Composable
fun BoardAndPieces(gameCtlr: IGameController)
{
	val ctx = LocalContext.current
	val showCapture = Pref.getBool(ctx, R.array.pf_showCaptured)
	val capturedBelow = Pref.getBool(ctx, R.array.pf_capturedBelow)
	val isGenChess = remember { gameCtlr.isGenChess }

	Column {
		if (capturedBelow) {
			if (isGenChess.value) {
				AndroidView({ gameCtlr.placeView })
				Spacer(modifier = Modifier.height(20.dp))
			}
		} else if (showCapture) {
			AndroidView({ gameCtlr.capturedView })
			Spacer(modifier = Modifier.height(10.dp))
		}

		AndroidView({ gameCtlr.boardView })

		if (capturedBelow) {
			if (showCapture) {
				Spacer(modifier = Modifier.height(10.dp))
				AndroidView({ gameCtlr.capturedView })
			}
		} else if (isGenChess.value) {
			Spacer(modifier = Modifier.height(20.dp))
			AndroidView({ gameCtlr.placeView })
		}
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
		Spacer(Modifier.aspectRatio(1.25f))
		content.invoke()
	}
}

@Composable
fun GameNav(gameCtlr: IGameController) {
	IconButton(onClick = { gameCtlr.onBackClick() }) {
		Icon(Icons.Filled.ArrowBack, "Back", Modifier.size(30.dp))
	}
	IconButton(onClick = { gameCtlr.onForwardClick() }) {
		Icon(Icons.Filled.ArrowForward, "Forward", Modifier.size(30.dp))
	}
	IconButton(onClick = { gameCtlr.onCurrentClick() }) {
		Icon(Icons.Filled.PlayArrow, "Last", Modifier.size(30.dp))
	}
}

@Composable
fun ShowGameDialogs(gameCtlr: IGameController) {
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
}

@Composable
fun ShowSubmitDialog(gameCtlr: GameController) {
	val submitState = remember { gameCtlr.submitState }
	if (!submitState.value.show) {
		return
	}

	val ctx = LocalContext.current

	Popup(alignment = Alignment.BottomCenter,
		onDismissRequest =  {
			gameCtlr.undoMove()
			submitState.value = SubmitState()
		}
	) {
		Row(
			modifier = Modifier
				.height(64.dp)
				.background(Color.Gray)
		) {
			OutlinedButton(
				onClick = {
					gameCtlr.undoMove()
					submitState.value = SubmitState()
				},
				modifier = Modifier
					.fillMaxWidth(.5f)
					.padding(12.dp)
			) {
				Text("Cancel", fontSize = 20.sp)
			}
			Button(
				onClick = {
					val move = submitState.value.move
					gameCtlr.submitMove(move)
					submitState.value = SubmitState()
				},
				modifier = Modifier
					.fillMaxWidth(1f)
					.padding(12.dp)
			) {
				Text("Submit", fontSize = 20.sp)
			}
		}
	}
}
