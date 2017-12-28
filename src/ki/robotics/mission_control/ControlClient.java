package ki.robotics.mission_control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Communication-Instance for the client-side.
 *
 * @version 1.1, 12/28/17
 */
public class ControlClient {

    private static final int TRANSMISSION_TIMEOUT = 0;

    private String host;
    private int port;
    private MCL monty;



    /**
     * Constructor.
     *
     * @param host  The host to which to connect.
     * @param port  The port to address.
     */
    public ControlClient(String host, int port) {
        this.host = host;
        this.port = port;
        monty = new MCL();
    }


    /**
     * Starts a connection-attempt to the server.
     * Once a connection is established an initial request is sent to the server. The connection is kept
     * until either this client or the server (robots) sends a 'SFIN'- (instruction Sequence FINished) or
     * 'DCNT'- (DisCoNnecT) signal.
     */
    public void start() {
        ArrayList<String> botResponses = new ArrayList<>();

        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);
            socket.setSoTimeout(TRANSMISSION_TIMEOUT);
            socket.setTcpNoDelay(true);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request = monty.execute(null);
            String response;

            do {
                out.println(request);
                do {
                    response = in.readLine();
                    if (response == null) {
                        continue;
                    }
                    botResponses.add(response);
                    if (response.contains("SFIN")) {
                        break;
                    }
                } while (true);
                request = monty.execute(botResponses);
            } while (!request.equals("DCNT"));

            out.println("DCNT");
            out.close();
            in.close();
            socket.close();
        } catch (SocketTimeoutException e) {

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
