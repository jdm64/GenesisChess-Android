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

import android.content.*
import android.content.ClipboardManager
import android.widget.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.compose.ui.window.*
import androidx.navigation.*
import com.chess.genesis.R
import com.chess.genesis.api.*
import com.chess.genesis.controller.*
import com.chess.genesis.data.*
import com.chess.genesis.data.Enums.GameSource
import kotlinx.coroutines.*
import java.util.*
import kotlin.jvm.optionals.getOrElse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePage(nav: NavHostController, source: String, gameId: String) {
	val state = rememberModalBottomSheetState()
	val ctx = LocalContext.current

	val gameSource = Enums.from(GameSource::class.java, source)
	val gameCtlr = remember { GameController(ctx, gameSource, gameId) }
	(ctx as MainActivity).currentController = gameCtlr

	LaunchedEffect(Unit) {
		PrefEdit(ctx).putString(R.array.pf_lastpage, "board/" + source + "/" + gameId).commit()
	}
	val scope = rememberCoroutineScope()

	DisposableEffect(gameCtlr) {
		onDispose {
			ctx.currentController = null
			gameCtlr.onDispose()
		}
	}

	GameContent(gameCtlr, state)

	if (state.isVisible) {
		ModalBottomSheet(
			onDismissRequest = { scope.launch { state.hide() } },
			sheetState = state
		) {
			GameMenu(gameCtlr, state, nav)
		}
	}

	ShowGenesisWarningDialog(ctx)

	ShowPromoteDialog(gameCtlr)

	ShowSubmitDialog(gameCtlr)

	ShowResignDialog(gameCtlr)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameMenu(gameCtlr: GameController, state: SheetState, nav: NavHostController) {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	Column {
		ListItem(
			modifier = Modifier.clickable(onClick = {
				val clipboard =
					ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				val clip = ClipData.newPlainText("simple text", gameCtlr.gameId)
				clipboard.setPrimaryClip(clip)
				Toast.makeText(ctx, "Game ID copied", Toast.LENGTH_SHORT).show()
				scope.launch { state.hide() }
			}),
			leadingContent = { Icon(Icons.Filled.Share, "Copy Game ID") },
			headlineContent = { Text("Copy Game ID") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				if (!nav.popBackStack("list/{mode}", false)) {
					val source = Optional.ofNullable(gameCtlr.source)
					val mode = source.getOrElse { GameSource.ACTIVE }
					nav.navigate("list/" + mode.name)
				}
				scope.launch { state.hide() }
			}),
			leadingContent = { Icon(Icons.AutoMirrored.Filled.List, "Game List") },
			headlineContent = { Text("Game List") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				scope.launch {
					ctx.startActivity(Intent(ctx, SettingsPage::class.java))
					state.hide()
				}
			}),
			leadingContent = { Icon(Icons.Filled.Settings, "Settings") },
			headlineContent = { Text("Settings") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				gameCtlr.getResignState().value = true
				scope.launch { state.hide() }
			}),
			leadingContent = { Icon(Icons.Filled.Flag, "Resign") },
			headlineContent = { Text("Resign") }
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(gameCtlr: IGameController, state: SheetState) {
	Column(
		Modifier
			.fillMaxHeight()
			.background(Color.Gray),
		verticalArrangement = Arrangement.SpaceBetween
	) {
		AndroidView({ gameCtlr.stmView })
		BoardAndPieces(gameCtlr)
		BottomBar(state) { GameNav(gameCtlr) }
	}
}

@Composable
fun BoardAndPieces(gameCtlr: IGameController) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(state: SheetState, content: @Composable () -> Unit) {
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
		Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.size(30.dp))
	}
	IconButton(onClick = { gameCtlr.onForwardClick() }) {
		Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward", Modifier.size(30.dp))
	}
	IconButton(onClick = { gameCtlr.onCurrentClick() }) {
		Icon(Icons.Filled.PlayArrow, "Last", Modifier.size(30.dp))
	}
}

@Composable
fun ShowPromoteDialog(gameCtlr: IGameController) {
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
			confirmButton = {
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

	if (Pref.getBool(LocalContext.current, R.array.pf_autoSubmitMove)) {
		val move = submitState.value.move
		gameCtlr.submitMove(move)
		submitState.value = SubmitState()
		return
	}

	Popup(alignment = Alignment.BottomCenter,
		onDismissRequest = {
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

@Composable
fun ShowResignDialog(gameCtlr: GameController) {
	val resignState = remember { gameCtlr.getResignState() }
	if (!resignState.value) {
		return
	}

	AlertDialog(onDismissRequest = { resignState.value = false },
		title = {
			Text(
				text = "Resign Game",
				fontWeight = FontWeight.Bold,
				fontSize = 20.sp
			)
		},
		text = {
			Text("Are you sure you want to resign this game?")
		},
		dismissButton = {
			TextButton(onClick = { resignState.value = false }) {
				Text("Cancel")
			}
		},
		confirmButton = {
			TextButton(onClick = {
				resignState.value = false
				gameCtlr.resign()
			}) {
				Text("Resign")
			}
		}
	)
}

@Composable
fun ShowGenesisWarningDialog(ctx: Context) {
	val show = remember { mutableStateOf(Pref.getBool(ctx, R.array.pf_showGenesisWarning)) }
	if (!show.value) {
		return;
	}

	AlertDialog(
		onDismissRequest = {
			show.value = false
		},
		title = {
			Text(
				text = "Genesis Chess Warning",
				fontWeight = FontWeight.Bold,
				fontSize = 20.sp,
				color = colorResource(R.color.red_A700)
			)
		},
		text = {
			Text(fontSize = 16.sp,
				text = "You are playing a chess variant called Genesis Chess. The board starts out empty and kings are placed first. Pawns are omnidirectional.")
		},
		confirmButton = {
			Button(onClick = {
				show.value = false
				PrefEdit(ctx).putBool(R.array.pf_showGenesisWarning, false).commit()
			}) {
				Text("Confirm")
			}
		}
	)
}
