package ki.robotics.rover;

import ki.robotics.rover.BotController;

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

    private int port;
    private boolean isSimulation;
    private BotController controller;

    private boolean stayOnline;
    private boolean stayConnected;


    /**
     * Constructor.
     *
     * @param port          The port to open for incomming connection-requests.
     * @param isSimulation  Boolean value to decide whether to use a physical or simulated robot.
     */
    public BotServer(int port, boolean isSimulation) {
        this.port = port;
        this.isSimulation = isSimulation;
        this.controller = new BotController(isSimulation);
    }



    /**
     * Starts the server and waits for an incoming connection.
     * Once a connection is established the client-requests (instructions to the robot) are handled.
     */
    public void powerUp() {
		stayOnline = true;
		stayConnected = true;
		while (stayOnline) {
            try {
                ServerSocket server = new ServerSocket(port);
                Socket client = server.accept();
                client.setKeepAlive(true);
                client.setSoTimeout(TIMEOUT);
                client.setTcpNoDelay(true);

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                controller.registerOutputStream(out);

                String request;

                do {
                    request = in.readLine();
                    if (request == null) {
                        continue;
                    }
                    stayConnected = controller.handleRequest(request);
                } while (stayConnected);

                out.close();
                in.close();
                client.close();
                server.close();
            } catch (SocketTimeoutException e) {

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
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
