package com.chess.genesis;

import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;

class NetDisconnect implements Runnable
{
	public void run()
	{
	try {
		Thread.sleep(2040);
		if (NetActive.get() < 1)
			SocketClient.hard_disconnect();
	} catch (InterruptedException e) {
	}
	}
}
