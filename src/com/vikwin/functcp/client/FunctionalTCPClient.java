package com.vikwin.functcp.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A client for 
 * {@link com.vikwin.functcp.server.FunctionalTCPServer FunctionalTCPServer}.
 * 
 * @author Viktor Winkelmann
 *
 * @param <Request>
 * 			Type of Request object
 * @param <Reply>
 * 			Type of Reply object
 */
public class FunctionalTCPClient<Request, Reply> {

	private Socket socket = null;
	private String host;
	private int port;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	/**
	 * Creates an instance of the Functional TCP Client that binds to a given
	 * host.
	 * 
	 * @param host
	 * 			Address of the server to connect to
	 * @param port
	 * 			Port to connect to
	 */
	public FunctionalTCPClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Connect to host.
	 * 
	 * @throws UnknownHostException
	 * 			Thrown if host cannot be resolved
	 * @throws IOException
	 * 			Thrown if I/O exception occurs on creating a socket
	 */
	private void connect() throws UnknownHostException, IOException {
		socket = new Socket(host, port);

		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Closes the connection.
	 */
	private void closeConnection() {
		try {
			input.close();
		} catch (IOException e) {
		}

		try {
			output.close();
		} catch (IOException e) {
		}
		
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Sends a request to the server and waits for a reply.
	 * This is used for synchronous messaging.
	 * If the connection is closed before a reply is received, the Reply object
	 * will be null.
	 * 
	 * @param request
	 * 			The Request object to send
	 * @return 
	 * 			Reply object from server or null 
	 * @throws UnknownHostException
	 * 			Thrown if host cannot be resolved
	 * @throws IOException
	 *			Thrown if I/O exception occurs on creating a socket
	 * @throws ClassNotFoundException
	 * 			Thrown if server Reply object is of unknown type
	 * 			
	 */
	@SuppressWarnings("unchecked")
	public synchronized Reply sendRepliedRequest(Request request)
			throws UnknownHostException, IOException, ClassNotFoundException {
		connect();

		Reply reply = null;

		output.writeObject(request);

		try {
			reply = (Reply) input.readObject();
		} catch (IOException e) {
		}	

		closeConnection();

		return reply;
	}

	/**
	 * Sends a request to the server without expecting a reply.
	 * This is used for asynchronous messaging.
	 * 
	 * @param request
	 * 			The Request object to send
	 * @throws UnknownHostException
	 * 			Thrown if host cannot be resolved
	 * @throws IOException
	 *			Thrown if I/O exception occurs on creating a socket
	 */
	public synchronized void sendNonRepliedRequest(Request request) throws UnknownHostException, IOException {
		connect();
		output.writeObject(request);
		closeConnection();
	}
}
