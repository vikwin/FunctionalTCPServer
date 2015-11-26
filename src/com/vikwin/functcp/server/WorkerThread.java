package com.vikwin.functcp.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Function;

/**
 * This class represents the threads that do the actual work.
 * 
 * @author Viktor Winkelmann
 *
 */
class WorkerThread<Request, Reply> extends Thread {
	private static final int SOCKETTIMEOUT = 60000;

	private final Function<Request, Reply> inputHandler;
	private Socket socket;

	public WorkerThread(Socket socket, Function<Request, Reply> inputHandler) {
		this.inputHandler = inputHandler;

		try {
			socket.setSoTimeout(SOCKETTIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.socket = socket;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			
			Reply reply;
			do {
				reply = inputHandler.apply((Request) input.readObject());
				
				if (reply != null)
					output.writeObject(reply);
			} while (input.available() > 0);
			
			input.close();
			output.close();
			socket.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
