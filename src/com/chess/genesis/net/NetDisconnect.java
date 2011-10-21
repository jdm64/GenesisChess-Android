package com.chess.genesis;

class NetDisconnect implements Runnable
{
	public void run()
	{
	try {
		Thread.sleep(2048);
		if (NetActive.get() < 1)
			SocketClient.disconnect();
	} catch (InterruptedException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
	}
}
