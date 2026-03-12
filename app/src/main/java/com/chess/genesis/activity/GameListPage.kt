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
import android.util.*
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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import androidx.paging.*
import androidx.paging.compose.*
import com.chess.genesis.R
import com.chess.genesis.data.*
import com.chess.genesis.data.Enums.*
import com.chess.genesis.db.*
import com.chess.genesis.net.*
import com.chess.genesis.util.*
import kotlinx.coroutines.*

object GVal {
    val dialogFontSize: TextUnit = 16.sp
}

class NewGameState {
	var show = mutableStateOf(false)
	var type = mutableStateOf(GameType.GENESIS)
	var opp = mutableStateOf(OpponentCat.MATCHED)
	var color = mutableStateOf(ColorType.RANDOM)
	var clockType = mutableStateOf(ClockType.REALTIME)
	var baseTime = mutableIntStateOf(ClockTimes.MIN_15.time)
	var incTime = mutableIntStateOf(ClockTimes.SEC_0.time)

	fun serialize(): String {
		val map = mapOf(
			"type" to type.value.id,
			"opp" to opp.value.id,
			"color" to color.value.id,
			"clockType" to clockType.value.id,
			"baseTime" to baseTime.intValue,
			"incTime" to incTime.intValue
		)
		return map.entries.joinToString(";") { "${it.key}=${it.value}" }
	}

	fun deserialize(str: String) {
		val map = str.split(";").associate {
			val (key, value) = it.split("=")
			key to value.toInt()
		}
		type.value = Enums.from(GameType::class.java, map["type"] ?: GameType.GENESIS.id)
		opp.value = Enums.from(OpponentCat::class.java, map["opp"] ?: OpponentCat.MATCHED.id)
		color.value = Enums.from(ColorType::class.java, map["color"] ?: ColorType.RANDOM.id)
		clockType.value = Enums.from(ClockType::class.java, map["clockType"] ?: ClockType.NO_CLOCK.id)
		baseTime.intValue = map["baseTime"] ?: 0
		incTime.intValue = map["incTime"] ?: 0
	}

	fun loadFromPrefs(context: Context) {
		val str = Pref(context).getString(R.array.pf_newGameState)
		if (str.isNotEmpty()) {
			try {
				deserialize(str)
			} catch (e: Exception) {
				Log.e("NewGameState", "Failed to load preferences: $str", e)
			}
		}
	}

	fun saveToPrefs(context: Context) {
		PrefEdit(context).putString(R.array.pf_newGameState, serialize()).commit()
	}
}

class EditGameState {
	var show = mutableStateOf(false)
	var name = mutableStateOf("")
	lateinit var data: ActiveGameEntity
}

class ImportGameState {
	var show = mutableStateOf(false)
	var id = mutableStateOf("")
}

fun onLoadGame(data: ActiveGameEntity, nav: NavHostController) {
	nav.navigate("board/active/" + data.gameid)
}

fun onNewGame(data: NewGameState, nav: NavHostController, context: Context) {
	data.saveToPrefs(context)
	data.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		when (data.opp.value) {
			OpponentCat.INVITE -> {
				ZeroMQClient.bind(context) { handler ->
					handler.createInvite(
						data.type.value,
						data.color.value,
						data.clockType.value,
						data.baseTime.intValue,
						data.incTime.intValue
					)
				}
			}
			OpponentCat.MATCHED -> {
				ZeroMQClient.bind(context) { handler ->
					handler.joinMatched(
						data.type.value,
						data.color.value,
						data.baseTime.intValue,
						data.incTime.intValue
					)
				}
			}
			else -> {
				val newGame = ActiveGameDao.get(context).newLocalGame(data)
				Dispatchers.Main.dispatch(Dispatchers.Main) {
					onLoadGame(newGame, nav)
				}
			}
		}
	}
}

fun onDeleteGame(state: EditGameState, context: Context) {
	state.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		ActiveGameDao.get(context).delete(state.data)
	}
}

fun onUpdateGame(state: EditGameState, context: Context) {
	state.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		val data = state.data
		data.name = state.name.value
		ActiveGameDao.get(context).update(data)
	}
}

fun onEditGame(state: MutableState<EditGameState>, data: ActiveGameEntity) {
	state.value.data = data
	state.value.name.value = data.name
	state.value.show.value = true
}

fun onImportGame(state: MutableState<ImportGameState>, context: Context) {
	state.value.show.value = false
	Dispatchers.IO.dispatch(Dispatchers.IO) {
		ZeroMQClient.bind(context) { handler -> handler.joinInvite(state.value.id.value) }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListPage(nav: NavHostController, mode: GameSource) {
	val ctx = LocalContext.current
	val isActive = mode == GameSource.ACTIVE

	LaunchedEffect(Unit) {
		PrefEdit(ctx).putString(R.array.pf_lastpage, "list/${mode.name}").commit()
	}

	if (isActive) {
		LaunchedEffect(Unit) {
			ZeroMQClient.bind(ctx) { client ->
				client.syncGames(SyncType.ACTIVE, 0)
			}
		}
	}

	val newGameState = remember { mutableStateOf(NewGameState()) }
	newGameState.value.loadFromPrefs(ctx)
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
				Spacer(Modifier.weight(1f))
				Text(
					text = if (isActive) "Active Games" else "Archive Games",
					style = MaterialTheme.typography.titleLarge,
					textAlign = TextAlign.Center
				)
				Spacer(Modifier.weight(1f))
				IconButton(onClick = {
					if (isActive) newGameState.value.show.value = true
				}) {
					if (isActive) {
						Icon(
							Icons.Filled.Add,
							"New Game",
							Modifier.size(30.dp)
						)
					}
				}
			}
		},
	) { padding ->
		GameList(nav, padding, mode)
	}

	if (showBottomSheet) {
		ModalBottomSheet(
			onDismissRequest = { showBottomSheet = false },
			sheetState = sheetState
		) {
			ListMenu(
				mode = mode,
				nav = nav,
				onHide = {
					coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
						if (!sheetState.isVisible) {
							showBottomSheet = false
						}
					}
				},
				importState = importState
			)
		}
	}

	if (isActive) {
		ShowNewGameDialog(newGameState, nav)
		ShowImportInviteDialog(importState)
	}
}

@Composable
fun ListMenu(
	mode: GameSource,
	nav: NavHostController,
	onHide: () -> Unit,
	importState: MutableState<ImportGameState>
) {
	val ctx = LocalContext.current
	val isActive = mode == GameSource.ACTIVE

	Column {
		if (isActive) {
			ListItem(
				modifier = Modifier.clickable(onClick = {
					importState.value.show.value = true
					onHide()
				}),
				leadingContent = { Icon(Icons.Filled.Email, "Import Invite Game") },
				headlineContent = { Text("Import Invite Game") }
			)
		}
		ListItem(
			modifier = Modifier.clickable(onClick = {
				nav.navigate(if (isActive) "list/archive" else "list/active")
				onHide()
			}),
			leadingContent = { Icon(if (isActive) Icons.Filled.Archive else Icons.Filled.List, if (isActive) "Archive Games" else "Active Games") },
			headlineContent = { Text(if (isActive) "Archive Games" else "Active Games") }
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
fun GameList(nav: NavHostController, padding: PaddingValues, mode: GameSource) {
	val context = LocalContext.current
	val isActive = mode == GameSource.ACTIVE
	val pager = remember(isActive) { Pager(PagingConfig(10)) {
		if (isActive) ActiveGameDao.get(context).allGames else ArchiveGameDao.get(context).allGames
	}}
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
				GameListCard(gamedata, editState, nav, isActive)
			}
		}
	}

	if (isActive) {
		ShowEditGameDialog(editState)
	}
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
				Text("Game Name:", modifier = Modifier.padding(bottom = 4.dp), fontSize = GVal.dialogFontSize)
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
fun GameListCard(
	data: GameEntity,
	state: MutableState<EditGameState>,
	nav: NavHostController,
	isActive: Boolean
) {
	val type = Enums.from(GameType::class.java, data.gametype).name
	val opponent = Enums.from(OpponentType::class.java, data.opponent).name
	val time = PrettyDate(data.stime).agoFormat()
	val details = "type: $type  opponent: $opponent"

	Card(
		modifier = Modifier
			.padding(16.dp, 16.dp, 16.dp, 0.dp)
			.fillMaxWidth()
			.combinedClickable(
				onClick = { nav.navigate("board/${data.source.name}/${data.gameid}") },
				onLongClick = { if (isActive) onEditGame(state, data as ActiveGameEntity) }
			),
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
						fontSize = GVal.dialogFontSize
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowNewGameDialog(data: MutableState<NewGameState>, nav: NavHostController) {
	val state = data.value
	if (!state.show.value) {
		return
	}
	val ctx = LocalContext.current
	var expandedType by remember { mutableStateOf(false) }
	var expandedOpp by remember { mutableStateOf(false) }
	var expandedColor by remember { mutableStateOf(false) }
	var expandedClock by remember { mutableStateOf(false) }
	var expandedBase by remember { mutableStateOf(false) }
	var expandedInc by remember { mutableStateOf(false) }

	AlertDialog(onDismissRequest = { state.show.value = false },
		title = { Text(text = "New Game", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
		text = {
			Column {
				ExposedDropdownMenuBox(
					expanded = expandedType,
					onExpandedChange = { expandedType = it }
				) {
					TextField(
						value = if (state.type.value == GameType.GENESIS) "Genesis" else "Regular",
						textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
						label = { Text("Game Type:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
						onValueChange = {},
						readOnly = true,
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
						modifier = Modifier.menuAnchor()
					)
					ExposedDropdownMenu(
						expanded = expandedType,
						onDismissRequest = { expandedType = false }
					) {
						DropdownMenuItem(
							text = { Text("Genesis") },
							onClick = {
								state.type.value = GameType.GENESIS
								expandedType = false
							}
						)
						DropdownMenuItem(
							text = { Text("Regular") },
							onClick = {
								state.type.value = GameType.REGULAR
								expandedType = false
							}
						)
					}
				}
				Spacer(modifier = Modifier.height(8.dp))
				ExposedDropdownMenuBox(
					expanded = expandedOpp,
					onExpandedChange = { expandedOpp = it }
				) {
					TextField(
						value = when (state.opp.value) {
							OpponentCat.INVITE -> "Invite"
							OpponentCat.HUMAN -> "Local"
							OpponentCat.CPU -> "Computer"
							OpponentCat.MATCHED -> "Matched"
						},
						textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
						label = { Text("Opponent Type:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
						onValueChange = {},
						readOnly = true,
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOpp) },
						modifier = Modifier.menuAnchor()
					)
					ExposedDropdownMenu(
						expanded = expandedOpp,
						onDismissRequest = { expandedOpp = false }
					) {
						DropdownMenuItem(
							text = { Text("Matched") },
							onClick = {
								state.opp.value = OpponentCat.MATCHED
								state.clockType.value = ClockType.REALTIME
								expandedOpp = false
							}
						)
						DropdownMenuItem(
							text = { Text("Invite") },
							onClick = {
								state.opp.value = OpponentCat.INVITE
								expandedOpp = false
							}
						)
						DropdownMenuItem(
							text = { Text("Local") },
							onClick = {
								state.opp.value = OpponentCat.HUMAN
								state.color.value = ColorType.RANDOM
								expandedOpp = false
							}
						)
						DropdownMenuItem(
							text = { Text("Computer") },
							onClick = {
								state.opp.value = OpponentCat.CPU
								expandedOpp = false
							}
						)
					}
				}
				Spacer(modifier = Modifier.height(8.dp))
				ExposedDropdownMenuBox(
					expanded = expandedColor,
					onExpandedChange = { expandedColor = it && state.opp.value != OpponentCat.HUMAN }
				) {
					TextField(
						value = when (state.color.value) {
							ColorType.RANDOM -> "Any"
							ColorType.BLACK -> "White"
							ColorType.WHITE -> "Black"
						},
						textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
						label = { Text("Play as Color:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
						onValueChange = {},
						readOnly = true,
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor) },
						modifier = Modifier.menuAnchor()
					)
					ExposedDropdownMenu(
						expanded = expandedColor,
						onDismissRequest = { expandedColor = false }
					) {
						DropdownMenuItem(
							text = { Text("Any") },
							enabled = state.opp.value != OpponentCat.HUMAN,
							onClick = {
								state.color.value = ColorType.RANDOM
								expandedColor = false
							}
						)
						DropdownMenuItem(
							text = { Text("White") },
							enabled = state.opp.value != OpponentCat.HUMAN,
							onClick = {
								state.color.value = ColorType.BLACK
								expandedColor = false
							}
						)
						DropdownMenuItem(
							text = { Text("Black") },
							enabled = state.opp.value != OpponentCat.HUMAN,
							onClick = {
								state.color.value = ColorType.WHITE
								expandedColor = false
							}
						)
					}
				}
				Spacer(modifier = Modifier.height(8.dp))
				ExposedDropdownMenuBox(
					expanded = expandedClock,
					onExpandedChange = { expandedClock = it && state.opp.value != OpponentCat.MATCHED }
				) {
					TextField(
						value = when (state.clockType.value) {
							ClockType.NO_CLOCK -> "No Clock"
							ClockType.REALTIME -> "Realtime"
							ClockType.PER_MOVE -> "Max Time per Move"
						},
						textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
						label = { Text("Clock Type:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
						onValueChange = {},
						readOnly = true,
						trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClock) },
						modifier = Modifier.menuAnchor()
					)
					ExposedDropdownMenu(
						expanded = expandedClock,
						onDismissRequest = { expandedClock = false }
					) {
						DropdownMenuItem(
							text = { Text("No Clock") },
							onClick = {
								state.clockType.value = ClockType.NO_CLOCK
								state.baseTime.intValue = ClockTimes.SEC_0.time
								state.incTime.intValue = ClockTimes.SEC_0.time
								expandedClock = false
							}
						)
						DropdownMenuItem(
							text = { Text("Realtime") },
							onClick = {
								state.clockType.value = ClockType.REALTIME
								state.baseTime.intValue = ClockTimes.MIN_15.time
								state.incTime.intValue = ClockTimes.SEC_5.time
								expandedClock = false
							}
						)
						DropdownMenuItem(
							text = { Text("Max Time per Move") },
							onClick = {
								state.clockType.value = ClockType.PER_MOVE
								state.baseTime.intValue = ClockTimes.DAY_1.time
								state.incTime.intValue = ClockTimes.SEC_0.time
								expandedClock = false
							}
						)
					}
				}

				if (state.clockType.value == ClockType.REALTIME) {
					Spacer(modifier = Modifier.height(8.dp))
					ExposedDropdownMenuBox(
						expanded = expandedBase,
						onExpandedChange = { expandedBase = it }
					) {
						TextField(
							value = ClockTimes.from(state.baseTime.intValue, ClockTimes.MIN_15),
							textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
							label = { Text("Base Time:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
							onValueChange = {},
							readOnly = true,
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBase) },
							modifier = Modifier.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = expandedBase,
							onDismissRequest = { expandedBase = false }
						) {
							listOf(
								ClockTimes.MIN_2,
								ClockTimes.MIN_3,
								ClockTimes.MIN_5,
								ClockTimes.MIN_10,
								ClockTimes.MIN_15,
								ClockTimes.MIN_30,
								ClockTimes.MIN_60,
								ClockTimes.MIN_90
							).forEach { time ->
								DropdownMenuItem(
									text = { Text(time.name) },
									onClick = {
										state.baseTime.intValue = time.time
										expandedBase = false
									}
								)
							}
						}
					}
					Spacer(modifier = Modifier.height(8.dp))
					ExposedDropdownMenuBox(
						expanded = expandedInc,
						onExpandedChange = { expandedInc = it }
					) {
						TextField(
							value = ClockTimes.from(state.incTime.intValue, ClockTimes.SEC_0),
							textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
							label = { Text("Increment:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
							onValueChange = {},
							readOnly = true,
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInc) },
							modifier = Modifier.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = expandedInc,
							onDismissRequest = { expandedInc = false }
						) {
							listOf(
								ClockTimes.SEC_0,
								ClockTimes.SEC_1,
								ClockTimes.SEC_2,
								ClockTimes.SEC_5,
								ClockTimes.SEC_10,
								ClockTimes.SEC_20
							).forEach { time ->
								DropdownMenuItem(
									text = { Text(time.name) },
									onClick = {
										state.incTime.intValue = time.time
										expandedInc = false
									}
								)
							}
						}
					}
				}
				if (state.clockType.value == ClockType.PER_MOVE) {
					Spacer(modifier = Modifier.height(8.dp))
					ExposedDropdownMenuBox(
						expanded = expandedBase,
						onExpandedChange = { expandedBase = it }
					) {
						TextField(
							value = ClockTimes.from(state.baseTime.intValue, ClockTimes.DAY_1),
							textStyle = TextStyle(fontSize = GVal.dialogFontSize, textAlign = TextAlign.End),
							label = { Text("Max Time per Move:", fontSize = GVal.dialogFontSize, fontStyle = Italic) },
							onValueChange = {},
							readOnly = true,
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBase) },
							modifier = Modifier.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = expandedBase,
							onDismissRequest = { expandedBase = false }
						) {
							listOf(
								ClockTimes.HRS_12,
								ClockTimes.DAY_1,
								ClockTimes.DAY_3
							).forEach { time ->
								DropdownMenuItem(
									text = { Text(time.name) },
									onClick = {
										state.baseTime.intValue = time.time
										expandedBase = false
									}
								)
							}
						}
					}
				}
			}
		},
		confirmButton = {
			Button(onClick = { onNewGame(state, nav, ctx) }) {
				Text("Create", fontSize = GVal.dialogFontSize)
			}
		},
		dismissButton = {
			OutlinedButton(onClick = { state.show.value = false }) {
				Text("Cancel", fontSize = GVal.dialogFontSize)
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