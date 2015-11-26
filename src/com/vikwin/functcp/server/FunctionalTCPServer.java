package com.vikwin.functcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * FunctionalTCPServer is a generic, multithreaded, message-based TCP Server.
 * It expects messages (Request objects) of any type from a client, passes it
 * to a user defined function, and optionally sends back a reply of any type 
 * (Reply object). 
 * The corresponding client is 
 * {@link com.vikwin.functcp.client.FunctionalTCPClient FunctionalTCPClient}.
 * 
 * @author Viktor Winkelmann
 *
 * @param <Request>
 *            Type of Request object
 * @param <Reply>
 *            Type of Reply object
 */
public class FunctionalTCPServer<Request, Reply> {
	private static final int DEFAULT_WORKER_THREADS = 20;

	private final Function<Request, Reply> inputHandler;
	private int port;

	private int workerThreads;
	private Thread mainThread;
	private ServerSocket socket = null;

	/**
	 * Creates an instance of the Functional TCP Server on a given port. If port
	 * is set to 0, it will start on a random free port. It can be retrieved
	 * afterwards using {@link #getPort()}.
	 * The instance has to be started using {@link #start()} after instantiation.  
	 * 
	 * @param inputHandler 	
	 * 			A handler function that processes message objects reveived
	 * 			from clients. 
	 * 
	 * @param port
	 * 			A port number to listen on (0-65535) 
	 */
	public FunctionalTCPServer(Function<Request, Reply> inputHandler, int port) {
		if (port < 0 || port > 65535)
			throw new IllegalArgumentException("Port number has to be between 0 and 65535!");
		
		this.port = port;
		this.inputHandler = inputHandler;
		workerThreads = DEFAULT_WORKER_THREADS;
	}

	/**
	 * Returns the maximum count of threads the server will be using.
	 * 
	 * @return 
	 * 			Maximum workerthread count
	 */
	public int getWorkerCount() {
		return workerThreads;
	}

	/**
	 * Sets the maximum count of threads the server will be using.
	 * 
	 * @param count
	 *			Maximum workerthread count
	 */
	public void setWorkerCount(int count) {
		workerThreads = count;
	}

	/**
	 * Returns the currently set port.
	 * 
	 * Note: If the server was instantiated with port set to 0, this
	 * method will return 0 until the server is started. 
	 * 
	 * @return Port the server listens on
	 */
	public int getPort() {
		if (socket != null && !socket.isClosed())
			return socket.getLocalPort();

		return port;
	}

	/**
	 * Checks if the server main thread is running.
	 * 
	 * @return
	 * 			true if running, else false
	 */
	public boolean isRunning() {
		if (mainThread == null || !mainThread.isAlive())
			return false;

		return true;
	}

	/**
	 * Starts the server in it's own thread.
	 * This is an asynchronous (non-blocking) call.
	 */
	public synchronized void start() {
		if (isRunning())
			return;
		
		mainThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// bind to socket
				try {
					socket = new ServerSocket(port);
				} catch (IOException e) {
					System.out.println("Could not bind to port " + port + " : "
							+ e.getMessage());
					System.exit(-1);
				}

				// create threadworker pool
				ExecutorService threadPool = Executors
						.newFixedThreadPool(workerThreads);

				// Wait for connections and dispatch work to workerthreads
				while (!Thread.currentThread().isInterrupted()) {
					Socket client;
					try {
						client = socket.accept();
						threadPool.execute(new WorkerThread<Request, Reply>(
								client, inputHandler));
					}

					catch (IOException e) {
						Thread.currentThread().interrupt();
					}
				}

				try {
					socket.close();
				} catch (IOException e1) {
				}

				threadPool.shutdown();

			}
		});

		mainThread.setName(getClass().getName() + " main thread");
		mainThread.start();
	}

	/**
	 * Shuts the server down if it is running.
	 * This is a synchronous (blocking) call.
	 * 
	 * @throws InterruptedException
	 * 			Thrown if current thread get's interrupted
	 */
	public void shutdown() throws InterruptedException {
		if (mainThread == null || !isRunning())
			return;

		try {
			socket.close();
		} catch (IOException e) {
		}
		
		mainThread.join();
	}
}
