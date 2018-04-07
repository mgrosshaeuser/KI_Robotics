package ki.robotics.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * Comunication-Instance for the Server-Side (e.g. the robot or a simulation).
 *
 * @version 1.1, 12/28/17
 */
public class BotServer {

    private static final int TIMEOUT = 0;

    private final int port;
    private ServerSocket server;
    private Socket client;

    private final BotController controller;

    private boolean stayOnline;
    private boolean stayConnected;


    /**
     * Constructor.
     *
     * @param port          The port to open for incoming connection-requests.
     * @param isSimulation  Boolean value to decide whether to use a physical or simulated robot.
     */
    public BotServer(int port, boolean isSimulation) {
        this.port = port;
        this.controller = new BotController(isSimulation, this);
    }



    /**
     * Starts the server and waits for an incoming connection.
     * Once a connection is established the client-requests (instructions to the robot) are handled.
     */
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


	private void createSockets(int port) throws IOException {
        server = new ServerSocket(port);
        client = server.accept();
        client.setKeepAlive(true);
        client.setSoTimeout(TIMEOUT);
        client.setTcpNoDelay(true);
    }

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

    private void tearDownConnection(BufferedReader in, PrintWriter out) throws IOException {
        controller.registerOutputStream(null);
        out.close();
        in.close();
        client.close();
        server.close();
    }


    /**
     * Allows the robot to disconnect from the client over a detour over the Main-class.
     */
	public void disconnect() {
		stayConnected = false;
	}



    /**
     * Allows the robot to shutdown the server over a detour over the Main-class.
     */
	public void shutdown() {
		stayOnline = false;
	}
}
