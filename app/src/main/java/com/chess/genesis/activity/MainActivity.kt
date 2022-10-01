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
import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.chess.genesis.R
import com.chess.genesis.data.*
import com.chess.genesis.db.*
import com.chess.genesis.net.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

	private var hasInviteGames = false
	private var serviceConnected = false

	private val connection = object : ZeroMQClient.LocalConnection() {
		override fun onServiceConnected(client: ZeroMQClient?) {
			serviceConnected = true
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Dispatchers.IO.dispatch(Dispatchers.IO) {
			hasInviteGames = LocalGameDao.get(this).hasInviteGame()
		}

		setContent {
			MainApp()
		}
	}

	override fun onStart() {
		super.onStart()
		if (hasInviteGames) {
			ZeroMQClient.bind(applicationContext, connection)
		}
	}

	override fun onStop() {
		if (serviceConnected) {
			applicationContext.unbindService(connection)
			serviceConnected = false
		}
		super.onStop()
	}
}

@Composable
fun MainApp() {
	val nav = rememberNavController()

	nav.addOnDestinationChangedListener { _, _, bundle ->
		val intent = bundle?.get("android-support-nav:controller:deepLinkIntent")
		if (intent is Intent) {
			val path = intent.data?.path?.substring(1)
			if (path != null) {
				PrefEdit(nav.context).putString(R.array.pf_lastpage, path).commit()
			}
		}
	}

	MaterialTheme {
		NavHost(nav, "start") {
			composable("start") { LoadingPage(nav) }
			composable("list") { GameListPage(nav) }
			composable("board/{gameId}") { entry ->
				val id = entry.arguments?.getString("gameId")
				if (id != null) {
					GamePage(nav, id)
				}
			}
		}
	}
}

@Composable
fun LoadingPage(nav: NavHostController) {
	Box(Modifier.fillMaxSize())

	LaunchedEffect(1) {
		Thread.sleep(125)
		val page = Pref.getString(nav.context, R.array.pf_lastpage)
		nav.popBackStack()
		nav.navigate(page)
	}
}
