package ki.robotics.server.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * Communication-Instance for the Server-Side (e.g. the robot or a simulation).
 */
public class ServerCommunicatorImpl implements ServerCommunicator {

    private static final int TIMEOUT = 0;

    private final int port;
    private ServerSocket server;
    private Socket client;

    private ServerComControllerImpl controller;

    private boolean stayOnline;
    private boolean stayConnected;


    /**
     * Constructor.
     *
     * @param port          The port to open for incoming connection-requests.
     */
    public ServerCommunicatorImpl(int port) {
        this.port = port;
    }


    /**
     * Registers a communication-controller as an intermediate between communicator and robot.
     *
     * @param comController A communication-controller
     */
    @Override
    public void registerComController(ServerComControllerImpl comController) {
        this.controller = comController;
    }


    /**
     * Starts the server and waits for an incoming connection.
     * Once a connection is established the client-requests (instructions to the robot) are handled.
     */
    @Override
    public void powerUp() {
		stayOnline = true;
		while (stayOnline) {
            stayConnected = true;
            try {
                createSockets(port);

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                controller.registerOutputStream(out);

                handleOngoingCommunication(in);
                tearDownConnection(in, out);
            } catch (SocketTimeoutException ignored) {

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
	}


    /**
     * Creates a new server-socket.
     *
     * @param port      Port for the socket
     * @throws IOException
     */
	private void createSockets(int port) throws IOException {
        server = new ServerSocket(port);
        client = server.accept();
        client.setKeepAlive(true);
        client.setSoTimeout(TIMEOUT);
        client.setTcpNoDelay(true);
    }


    /**
     * Handles ongoing communication by reading transmissions from the input-stream and forwarding them to
     * the communication-controller (instance of ServerComController).
     *
     * @param in    The BufferedReader for the input-stream
     * @throws IOException
     */
    private void handleOngoingCommunication(BufferedReader in) throws IOException {
        String request;
        do {
            request = in.readLine();
            if (request == null) {
                continue;
            }
            controller.handleRequest(request);
        } while (stayConnected);
    }


    /**
     * Resets the output-stream known to the communication-controller to null and closes the server-side.
     *
     * @param in    The BufferedReader for the input-stream.
     * @param out   The PrintWriter for the ouput-stream.
     * @throws IOException
     */
    private void tearDownConnection(BufferedReader in, PrintWriter out) throws IOException {
        controller.registerOutputStream(null);
        out.close();
        in.close();
        client.close();
        server.close();
    }


    /**
     * Allows the robot to disconnect from the client over a detour over its communication-controller.
     */
	@Override
    public void disconnect() {
		stayConnected = false;
	}



    /**
     * Allows the robot to shutdown the server over a detour over its communication-controller.
     */
	@Override
    public void shutdown() {
		stayOnline = false;
	}
}
