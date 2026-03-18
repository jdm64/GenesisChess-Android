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

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.chess.genesis.R
import com.chess.genesis.controller.*
import com.chess.genesis.data.*
import com.chess.genesis.data.Enums.*
import com.chess.genesis.net.*

class MainActivity : ComponentActivity() {
	var currentController: GameController? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			GenesisChessTheme {
				MainApp()
			}
		}
	}

	override fun onResume() {
		ZeroMQClient.setAppActive(true)
		currentController?.onResume()
		super.onResume()
	}

	override fun onPause() {
		ZeroMQClient.setAppActive(false)
		super.onPause()
	}

	override fun onDestroy() {
		currentController?.onDispose()
		currentController = null
		super.onDestroy()
	}
}

@Composable
fun MainApp() {
	val nav = rememberNavController()

	NavHost(nav, "start", modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
		composable("start") { LoadingPage(nav) }
		composable("list/{mode}") { entry ->
			val mode = entry.arguments?.getString("mode")?.let {
				Enums.from(GameSource::class.java, it)
			} ?: GameSource.ACTIVE
			GameListPage(nav, mode)
		}
		composable("board/{source}/{gameId}") { entry ->
			val source = entry.arguments?.getString("source")
			val id = entry.arguments?.getString("gameId")
			if (source != null && id != null) {
				GamePage(nav, source, id)
			}
		}
	}
}

@Composable
fun LoadingPage(nav: NavHostController) {
	Box(Modifier.fillMaxSize())

	LaunchedEffect(1) {
		Thread.sleep(125)
		var page = Pref.getString(nav.context, R.array.pf_lastpage)
		if (page.equals("list")) {
			page = "list/active"
		}
		nav.popBackStack()
		nav.navigate(page)
	}
}
