/* GenChess, a genesis chess engine
 * Copyright (C) 2022, Justin Madru (justin.jdm64@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chess.genesis.net;

import com.chess.genesis.R;
import com.chess.genesis.data.*;
import com.google.firebase.messaging.*;
import androidx.annotation.*;

public class NotificationService extends FirebaseMessagingService
{
	@Override
	public void onMessageReceived(RemoteMessage message)
	{
//		var data = message.getData();
//		Log.v(getClass().getSimpleName(), "Message: " + data.toString());
	}

	@Override
	public void onNewToken(@NonNull String token)
	{
		new PrefEdit(getApplicationContext()).putString(R.array.pf_firebaseToken, token).commit();
	}
}
