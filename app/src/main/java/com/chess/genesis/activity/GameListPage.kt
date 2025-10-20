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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import androidx.paging.*
import androidx.paging.compose.*
import com.chess.genesis.R
import com.chess.genesis.data.*
import com.chess.genesis.db.*
import com.chess.genesis.net.*
import com.chess.genesis.util.*
import kotlinx.coroutines.*

class NewGameState {
	var show = mutableStateOf(false)
	var name = mutableStateOf("Untitled")
	var type = mutableIntStateOf(Enums.GENESIS_CHESS)
	var opp = mutableIntStateOf(Enums.INVITE_OPPONENT)
	var color = mutableIntStateOf(Enums.RANDOM_OPP)
}

class EditGameState {
	var show = mutableStateOf(false)
	var name = mutableStateOf("")
	lateinit var data: LocalGameEntity
}

class ImportGameState {
	var show = mutableStateOf(false)
	var id = mutableStateOf("")
}

fun onLoadGame(data: LocalGameEntity, nav: NavHostController) {
	nav.navigate("board/" + data.gameid)
}

fun onNewGame(data: NewGameState, nav: NavHostController, context: Context) {
	data.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		if (data.opp.intValue == Enums.INVITE_OPPONENT) {
			val playAs = Enums.OppToPlayAs(data.opp.intValue)
			ZeroMQClient.bind(context) { client ->
				client.createInvite(
					data.type.intValue,
					playAs
				)
			}
		} else {
			val newGame = LocalGameDao.get(context).newLocalGame(data)
			Dispatchers.Main.dispatch(Dispatchers.Main) {
				onLoadGame(newGame, nav)
			}
		}
	}
}

fun onDeleteGame(state: EditGameState, context: Context) {
	state.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		LocalGameDao.get(context).delete(state.data)
	}
}

fun onUpdateGame(state: EditGameState, context: Context) {
	state.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		val data = state.data
		data.name = state.name.value
		LocalGameDao.get(context).update(data)
	}
}

fun onEditGame(state: MutableState<EditGameState>, data: LocalGameEntity) {
	state.value.data = data
	state.value.name.value = data.name
	state.value.show.value = true
}

fun onImportGame(state: MutableState<ImportGameState>, context: Context) {
	state.value.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		ZeroMQClient.bind(context) { client -> client.joinInvite(state.value.id.value) }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListPage(nav: NavHostController) {
	val newGameState = remember { mutableStateOf(NewGameState()) }
	val sheetState = rememberModalBottomSheetState()
	val coroutineScope = rememberCoroutineScope()
	val importState = remember { mutableStateOf(ImportGameState()) }
	var showBottomSheet by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
				containerColor = colorResource(R.color.blue_800),
				titleContentColor = colorResource(R.color.blue_800),
			), title = {
				Image(
					modifier = Modifier
						.fillMaxWidth()
						.height(26.dp),
					painter = painterResource(R.drawable.genesischess),
					contentDescription = "Genesis Chess"
				)
			})
		},
		bottomBar = {
			BottomAppBar {
				IconButton(onClick = { showBottomSheet = true }) {
					Icon(
						Icons.Filled.Menu,
						"menu",
						Modifier.size(30.dp)
					)
				}
				Spacer(Modifier.weight(1f, true))
				IconButton(onClick = {
					newGameState.value.show.value = true
				}) {
					Icon(
						Icons.Filled.Add,
						"New Game",
						Modifier.size(30.dp)
					)
				}
			}
		},
	) { padding ->
		LocalGameList(nav, padding)
	}

	if (showBottomSheet) {
		ModalBottomSheet(
			onDismissRequest = { showBottomSheet = false },
			sheetState = sheetState
		) {
			ListMenu(onHide = {
				coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
					if (!sheetState.isVisible) {
						showBottomSheet = false
					}
				}
			}, importState = importState)
		}
	}

	ShowNewGameDialog(newGameState, nav)

	ShowImportInviteDialog(importState)
}

@Composable
fun ListMenu(onHide: () -> Unit, importState: MutableState<ImportGameState>) {
	val ctx = LocalContext.current

	Column {
		ListItem(
			modifier = Modifier.clickable(onClick = {
				importState.value.show.value = true
				onHide()
			}),
			leadingContent = { Icon(Icons.Filled.Email, "Import Invite Game") },
			headlineContent = { Text("Import Invite Game") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				ctx.startActivity(Intent(ctx, SettingsPage::class.java))
				onHide()
			}),
			leadingContent = { Icon(Icons.Filled.Settings, "Settings") },
			headlineContent = { Text("Settings") }
		)
	}
}

@Composable
fun LocalGameList(nav: NavHostController, padding: PaddingValues) {
	val context = LocalContext.current
	val pager = remember { Pager(PagingConfig(10)) { LocalGameDao.get(context).allGames } }
	val lazyItems = pager.flow.collectAsLazyPagingItems()
	val editState = remember { mutableStateOf(EditGameState()) }

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.padding(padding)
	) {
		items(lazyItems.itemCount, lazyItems.itemKey { it.gameid }) { index ->
			val gamedata = lazyItems[index]
			if (gamedata != null) {
				LocalGameItem(gamedata, editState, nav)
			}
		}
	}

	ShowEditGameDialog(editState)
}

@Composable
fun ShowEditGameDialog(editState: MutableState<EditGameState>) {
	if (!editState.value.show.value) {
		return
	}
	val context = LocalContext.current

	AlertDialog(onDismissRequest = { editState.value.show.value = false },
		title = {
			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
				Text(text = "Edit", fontWeight = FontWeight.Bold, fontSize = 20.sp)
				Spacer(modifier = Modifier.weight(1f, true))
				Button(
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
					onClick = { onDeleteGame(editState.value, context) }
				) {
					Text("Delete")
				}
			}
		},
		text = {
			Column {
				Text("Game Name:", modifier = Modifier.padding(bottom = 4.dp))
				TextField(
					value = editState.value.name.value,
					onValueChange = { editState.value.name.value = it })
			}
		},
		confirmButton = {
			Button(onClick = { onUpdateGame(editState.value, context) }) {
				Text("Rename")
			}
		},
		dismissButton = {
			OutlinedButton(onClick = { editState.value.show.value = false }) {
				Text("Cancel")
			}
		},
	)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalGameItem(
	data: LocalGameEntity,
	state: MutableState<EditGameState>,
	nav: NavHostController
) {
	val type = Enums.GameType(data.gametype)
	val opponent = Enums.OpponentType(data.opponent)
	val time = PrettyDate(data.stime).agoFormat()
	val details = "type: $type  opponent: $opponent"

	Card(
		modifier = Modifier
			.padding(16.dp, 16.dp, 16.dp, 0.dp)
			.fillMaxWidth()
			.combinedClickable(onClick = { onLoadGame(data, nav) },
				onLongClick = { onEditGame(state, data) }),
		elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(end = 4.dp)
		) {
			Button(onClick = {}, modifier = Modifier.padding(start = 4.dp)) {
				Text(data.lastMoveTo(), fontSize = 20.sp)
			}
			Column(modifier = Modifier.padding(start = 8.dp)) {
				Row(
					Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = data.name,
						fontWeight = FontWeight.Bold,
						fontSize = 18.sp
					)
					Text(time, fontSize = 12.sp)
				}
				Text(
					details,
					fontSize = 14.sp,
					fontStyle = FontStyle.Italic
				)
			}
		}
	}
}

@Composable
fun ShowNewGameDialog(data: MutableState<NewGameState>, nav: NavHostController) {
	val state = data.value
	if (!state.show.value) {
		return
	}
	val ctx = LocalContext.current

	AlertDialog(onDismissRequest = { state.show.value = false },
		title = { Text(text = "New Game", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
		text = {
			Column {
				Text("Game Type:", fontWeight = FontWeight.Bold)
				Row(verticalAlignment = Alignment.CenterVertically) {
					RadioButton(
						selected = state.type.intValue == Enums.GENESIS_CHESS,
						onClick = {
							state.type.intValue = Enums.GENESIS_CHESS
						},
					)
					Text("Genesis")
					Spacer(modifier = Modifier.width(6.dp))
					RadioButton(
						selected = state.type.intValue == Enums.REGULAR_CHESS,
						onClick = {
							state.type.intValue = Enums.REGULAR_CHESS
						},
					)
					Text("Regular")
				}
				Text(
					"Opponent Type:",
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(top = 8.dp)
				)
				Row(verticalAlignment = Alignment.CenterVertically) {
					RadioButton(
						selected = state.opp.intValue == Enums.INVITE_OPPONENT,
						onClick = {
							state.opp.intValue = Enums.INVITE_OPPONENT
						},
					)
					Text("Invite")
					Spacer(modifier = Modifier.width(6.dp))
					RadioButton(
						selected = state.opp.intValue == Enums.HUMAN_OPPONENT,
						onClick = {
							state.opp.intValue = Enums.HUMAN_OPPONENT
						},
					)
					Text("Local")
					Spacer(modifier = Modifier.width(6.dp))
					RadioButton(
						selected = state.opp.intValue == Enums.CPU_OPPONENT,
						onClick = {
							state.opp.intValue = Enums.CPU_OPPONENT
						},
					)
					Text("Computer")
				}
				Text(
					"Play as Color:",
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(top = 8.dp)
				)
				Row(verticalAlignment = Alignment.CenterVertically) {
					RadioButton(
						enabled = state.opp.intValue != Enums.HUMAN_OPPONENT,
						selected = state.color.intValue == Enums.RANDOM_OPP,
						onClick = {
							state.color.intValue = Enums.RANDOM_OPP
						},
					)
					Text("Random")
					Spacer(modifier = Modifier.width(6.dp))
					RadioButton(
						enabled = state.opp.intValue != Enums.HUMAN_OPPONENT,
						selected = state.color.intValue == Enums.BLACK_OPP,
						onClick = {
							state.color.intValue = Enums.BLACK_OPP
						},
					)
					Text("White")
					Spacer(modifier = Modifier.width(6.dp))
					RadioButton(
						enabled = state.opp.intValue != Enums.HUMAN_OPPONENT,
						selected = state.color.intValue == Enums.WHITE_OPP,
						onClick = {
							state.color.intValue = Enums.WHITE_OPP
						},
					)
					Text("Black")
				}
				Text(
					"Name:",
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(top = 8.dp)
				)
				Spacer(modifier = Modifier.height(8.dp))
				TextField(
					value = state.name.value,
					onValueChange = { state.name.value = it })
			}
		},
		confirmButton = {
			TextButton(onClick = { onNewGame(state, nav, ctx) }) {
				Text("Create")
			}
		},
		dismissButton = {
			TextButton(onClick = { state.show.value = false }) {
				Text("Cancel")
			}
		}
	)
}

@Composable
fun ShowImportInviteDialog(state: MutableState<ImportGameState>) {
	if (!state.value.show.value) {
		return
	}
	val context = LocalContext.current

	AlertDialog(onDismissRequest = { state.value.show.value = false },
		title = {
			Text(
				text = "Import Invite Game",
				fontWeight = FontWeight.Bold,
				fontSize = 20.sp
			)
		},
		text = {
			Column {
				Text("Enter Game ID:", fontWeight = FontWeight.Bold)
				Spacer(modifier = Modifier.height(8.dp))
				TextField(
					value = state.value.id.value,
					onValueChange = { state.value.id.value = it })
			}
		},
		confirmButton = {
			TextButton(onClick = { onImportGame(state, context) }) {
				Text("Import")
			}
		},
		dismissButton = {
			TextButton(onClick = { state.value.show.value = false }) {
				Text("Cancel")
			}
		}
	)
}