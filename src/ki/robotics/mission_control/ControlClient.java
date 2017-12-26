package ki.robotics.mission_control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;


/**
 * Communication-Instance for the client-side.
 *
 * @version 1.0, 12/26/17
 */
public class ControlClient {

    private static final int TRANSMISSION_TIMEOUT = 500;

    private String host;
    private int port;
    private boolean stayConnected;


    /**
     * Constructor.
     *
     * @param host  The host to which to connect.
     * @param port  The port to address.
     */
    public ControlClient(String host, int port) {
        this.host = host;
        this.port = port;
    }



    /**
     *  Initiates the connection to the specified server (e.g. a robot or a simulation)
     *  At the moment, the only interaction is via an interactive shell.
     */
    public void powerUp() {
        stayConnected = true;
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);
            socket.setSoTimeout(TRANSMISSION_TIMEOUT);
            socket.setTcpNoDelay(true);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            String request;
            String response = null;

            while (stayConnected) {
                if (response == null  ||  response.equals("SFIN")) {
                    System.out.print("mission control ~$ ");
                    request = scanner.nextLine();
                    if (request.equals("exit")) {
                        stayConnected = false;
                        continue;
                    }
                    out.println(request);
                }

                try {
                    response = in.readLine();
                    if (response == null) {  continue;  }
                    System.out.println(">>> " + response);
                } catch (SocketTimeoutException e) {

                }
            }
            socket.close();
        } catch (SocketTimeoutException e1) {
            e1.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
