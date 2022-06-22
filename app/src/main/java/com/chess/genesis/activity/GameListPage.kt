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

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.chess.genesis.R
import com.chess.genesis.data.Enums
import com.chess.genesis.db.LocalGameDao
import com.chess.genesis.db.LocalGameEntity
import com.chess.genesis.net.AdhocMqttClient
import com.chess.genesis.util.PrettyDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewGameState {
	var show = mutableStateOf(false)
	var name = mutableStateOf("Untitled")
	var type = mutableStateOf(Enums.GENESIS_CHESS)
	var opp = mutableStateOf(Enums.CPU_OPPONENT)
	var color = mutableStateOf(Enums.BLACK_OPP)
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

fun onLoadGame(data: LocalGameEntity, nav: NavHostController, context: Context) {
	nav.navigate("board/" + data.gameid)
}

fun onNewGame(data: NewGameState, nav: NavHostController, context: Context) {
	data.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		val newGame = LocalGameDao.get(context).newLocalGame(data)
		if (newGame.opponent == Enums.INVITE_WHITE_OPPONENT || newGame.opponent == Enums.INVITE_BLACK_OPPONENT) {
			AdhocMqttClient.bind(context) { client -> client.sendInvite(newGame) }
		}
		Dispatchers.Main.dispatch(Dispatchers.Main) {
			onLoadGame(newGame, nav, context)
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
		var data = state.data
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
		AdhocMqttClient.bind(context) { client -> client.listenInvite(state.value.id.value) }
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GameListPage(nav: NavHostController) {
	val newGameState = remember { mutableStateOf(NewGameState()) }
	val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	val scaffoldState = rememberScaffoldState()
	val coroutineScope = rememberCoroutineScope()
	val importState = remember { mutableStateOf(ImportGameState()) }

	ModalBottomSheetLayout(
		sheetElevation = 16.dp,
		sheetShape = RoundedCornerShape(32.dp),
		sheetState = sheetState,
		sheetContent = { ListMenu(sheetState, importState) })
	{
		Scaffold(
			scaffoldState = scaffoldState,
			topBar = {
				TopAppBar {
					Row(verticalAlignment = Alignment.CenterVertically) {
						IconButton(onClick = { coroutineScope.launch { sheetState.show() } }) {
							Icon(
								Icons.Filled.Menu,
								"menu",
								Modifier.size(30.dp)
							)
						}
						Image(
							modifier = Modifier
								.fillMaxWidth()
								.height(26.dp),
							painter = painterResource(R.drawable.genesischess),
							contentDescription = "Genesis Chess"
						)
					}
				}
			},
			floatingActionButton = {
				FloatingActionButton(onClick = {
					newGameState.value.show.value = true
				}) {
					Icon(Icons.Filled.Add, "New Game")
				}
			},
			content = { LocalGameList(nav) },
		)
	}

	showNewGameDialog(newGameState, nav)

	showImportInviteDialog(importState)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListMenu(sheetState: ModalBottomSheetState, importState: MutableState<ImportGameState>) {
	var ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	Column {
		ListItem(
			modifier = Modifier.clickable(onClick = {
				scope.launch {
					importState.value.show.value = true
					sheetState.hide()
				}
			}),
			icon = { Icon(Icons.Filled.Email, "Import Invite Game") },
			text = { Text("Import Invite Game") }
		)
		ListItem(
			modifier = Modifier.clickable(onClick = {
				scope.launch {
					ctx.startActivity(Intent(ctx, SettingsPage::class.java))
					sheetState.hide()
				}
			}),
			icon = { Icon(Icons.Filled.Settings, "Settings") },
			text = { Text("Settings") }
		)
	}
}

@Composable
fun LocalGameList(nav: NavHostController) {
	var context = LocalContext.current
	var pager = remember { Pager(PagingConfig(10)) { LocalGameDao.get(context).allGames } }
	var lazyItems = pager.flow.collectAsLazyPagingItems()
	var editState = remember { mutableStateOf(EditGameState()) }

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.padding(bottom = 8.dp)
	) {
		items(lazyItems) { gamedata ->
			if (gamedata != null) {
				LocalGameItem(gamedata, editState, nav)
			}
		}
	}

	showEditGameDialog(editState)
}

@Composable
fun showEditGameDialog(editState: MutableState<EditGameState>) {
	if (!editState.value.show.value) {
		return
	}
	var context = LocalContext.current

	AlertDialog(onDismissRequest = { editState.value.show.value = false },
		title = {
			Text(text = "Edit", fontWeight = FontWeight.Bold, fontSize = 20.sp)
		},
		text = {
			Column {
				Text("Game Name:", modifier = Modifier.padding(bottom = 4.dp))
				TextField(
					value = editState.value.name.value,
					onValueChange = { editState.value.name.value = it })
			}
		},
		buttons = {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Button(colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
					onClick = { onDeleteGame(editState.value, context) }
				) {
					Text("Delete")
				}
				OutlinedButton(onClick = { editState.value.show.value = false }) {
					Text("Cancel")
				}
				Button(colors = ButtonDefaults.buttonColors(),
					onClick = { onUpdateGame(editState.value, context) }) {
					Text("Rename")
				}
			}
		}
	)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun LocalGameItem(
	data: LocalGameEntity,
	state: MutableState<EditGameState>,
	nav: NavHostController
) {
	var type = Enums.GameType(data.gametype)
	var opponent = Enums.OpponentType(data.opponent)
	var time = PrettyDate(data.stime).agoFormat()
	var details = "type: $type  opponent: $opponent"
	var ctx = LocalContext.current

	Card(
		modifier = Modifier
			.padding(16.dp, 16.dp, 16.dp, 0.dp)
			.fillMaxWidth()
			.combinedClickable(onClick = { onLoadGame(data, nav, ctx) },
				onLongClick = { onEditGame(state, data) }),
		elevation = 8.dp,
		content = {
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
	)
}

@Composable
fun showNewGameDialog(data: MutableState<NewGameState>, nav: NavHostController) {
	var state = data.value
	if (!state.show.value) {
		return
	}
	var ctx = LocalContext.current

	AlertDialog(onDismissRequest = { state.show.value = false },
		title = { Text(text = "New Game", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
		text = {
			Column {
				Text("Game Type:", fontWeight = FontWeight.Bold)
				Row(verticalAlignment = Alignment.CenterVertically) {
					RadioButton(
						selected = state.type.value == Enums.GENESIS_CHESS,
						onClick = {
							state.type.value = Enums.GENESIS_CHESS
						},
						modifier = Modifier.size(36.dp)
					)
					Text("Genesis")
					Spacer(modifier = Modifier.width(8.dp))
					RadioButton(
						selected = state.type.value == Enums.REGULAR_CHESS,
						onClick = {
							state.type.value = Enums.REGULAR_CHESS
						},
						modifier = Modifier.size(36.dp)
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
						selected = state.opp.value == Enums.CPU_OPPONENT,
						onClick = {
							state.opp.value = Enums.CPU_OPPONENT
						},
						modifier = Modifier.size(36.dp)
					)
					Text("CPU")
					Spacer(modifier = Modifier.width(8.dp))
					RadioButton(
						selected = state.opp.value == Enums.HUMAN_OPPONENT,
						onClick = {
							state.opp.value = Enums.HUMAN_OPPONENT
						},
						modifier = Modifier.size(36.dp)
					)
					Text("Local")
					Spacer(modifier = Modifier.width(8.dp))
					RadioButton(
						selected = state.opp.value == Enums.INVITE_OPPONENT,
						onClick = {
							state.opp.value = Enums.INVITE_OPPONENT
						},
						modifier = Modifier.size(36.dp)
					)
					Text("Invite")
				}
				Text(
					"Play as Color:",
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(top = 8.dp)
				)
				Row(verticalAlignment = Alignment.CenterVertically) {
					RadioButton(
						enabled = state.opp.value != Enums.HUMAN_OPPONENT,
						selected = state.color.value == Enums.BLACK_OPP,
						onClick = {
							state.color.value = Enums.BLACK_OPP
						},
						modifier = Modifier.size(36.dp)
					)
					Text("White")
					Spacer(modifier = Modifier.width(8.dp))
					RadioButton(
						enabled = state.opp.value != Enums.HUMAN_OPPONENT,
						selected = state.color.value == Enums.WHITE_OPP,
						onClick = {
							state.color.value = Enums.WHITE_OPP
						},
						modifier = Modifier.size(36.dp)
					)
					Text("Black")
					Spacer(modifier = Modifier.width(8.dp))
					RadioButton(
						enabled = state.opp.value != Enums.HUMAN_OPPONENT,
						selected = state.color.value == Enums.RANDOM_OPP,
						onClick = {
							state.color.value = Enums.RANDOM_OPP
						},
						modifier = Modifier.size(36.dp)
					)
					Text("Random")
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
		buttons = {
			Row(
				Modifier
					.fillMaxWidth()
					.padding(8.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				TextButton(onClick = { state.show.value = false }) {
					Text("Cancel")
				}
				TextButton(onClick = { onNewGame(state, nav, ctx) }) {
					Text("Create")
				}
			}
		}
	)
}

@Composable
fun showImportInviteDialog(state: MutableState<ImportGameState>) {
	if (!state.value.show.value) {
		return
	}
	var context = LocalContext.current

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
		buttons = {
			Row(
				Modifier
					.fillMaxWidth()
					.padding(8.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				TextButton(onClick = { state.value.show.value = false }) {
					Text("Cancel")
				}
				TextButton(onClick = { onImportGame(state, context) }) {
					Text("Import")
				}
			}
		}
	)
}