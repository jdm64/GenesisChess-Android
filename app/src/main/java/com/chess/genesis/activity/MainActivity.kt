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
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chess.genesis.view.BoardView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContent {
			MainApp()
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp()
{
	var ctx = LocalContext.current
	var theme = if (Build.VERSION.SDK_INT < 31) {
		if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
	} else {
		if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
	}
	var controller = rememberNavController()

	MaterialTheme(colorScheme = theme) {
		NavHost(navController = controller, startDestination = "board") {
			composable("board") { BoardPage(theme, controller) }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardPage(theme: ColorScheme, controller: NavController)
{
	var ctx = LocalContext.current
	var state = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	Scaffold(
		bottomBar = {
			BottomAppBar(modifier = Modifier.height(60.dp)) {
				IconButton(
					onClick = { scope.launch {
						if (state.isOpen)
							state.close()
						else state.open()
					}}
				) {
					Icon(Icons.Filled.Menu, "menu", Modifier.size(30.dp))
				}
				Spacer(Modifier.aspectRatio(1.0f))
				GameNav()
			}
		}
	) {
		AndroidView({ BoardView(ctx, null) })
		AppDrawer(state, theme, controller)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(state: DrawerState, theme: ColorScheme, controller: NavController)
{
	var ctx = LocalContext.current

	ModalNavigationDrawer(
		modifier = Modifier.width(225.dp),
		drawerState = state,
		drawerContent = {
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.background(theme.primary)
					.fillMaxWidth()
					.height(75.dp)) {
				Text(text = "Genesis Chess", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
			}
			NavigationDrawerItem(
				label = { Text("Game List") },
				selected = false,
				onClick = { /*TODO*/ },
				icon = { Icon(Icons.Filled.List, "Game List") })
			Divider(color = theme.primaryContainer)
			NavigationDrawerItem(
				label = { Text("Settings") },
				selected = false,
				onClick = { ctx.startActivity(Intent(ctx, SettingsPage::class.java)) },
				icon = { Icon(Icons.Filled.Settings, "Settings") })
		}
	) {}
}

@Composable
fun GameNav()
{
	IconButton(
		onClick = { /*TODO*/ }
	) {
		Icon(Icons.Filled.ArrowBack, "Back", Modifier.size(30.dp))
	}
	IconButton(
		onClick = { /*TODO*/ }
	) {
		Icon(Icons.Filled.ArrowForward, "Forward", Modifier.size(30.dp))
	}
	IconButton(
		onClick = { /*TODO*/ }
	) {
		Icon(Icons.Filled.PlayArrow, "Last", Modifier.size(30.dp))
	}
	IconButton(
		onClick = { /*TODO*/ }
	) {
		Icon(Icons.Filled.Place, "Place", Modifier.size(30.dp))
	}
}