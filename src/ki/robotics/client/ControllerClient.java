package ki.robotics.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static ki.robotics.utility.crisp.CRISP.BOT_DISCONNECT;
import static ki.robotics.utility.crisp.CRISP.INSTRUCTION_SEQUENCE_FINISHED;


/**
 * Communication-Instance for the client-side.
 *
 * @version 1.2, 01/02/18
 */
public final class ControllerClient implements Runnable{

    private static final int TRANSMISSION_TIMEOUT = 0;

    private String host;
    private int port;
    private Controller controller;

    public static volatile boolean running = true;


    /**
     * Constructor.
     *
     * @param host  The host to which to connect.
     * @param port  The port to address.
     */
    public ControllerClient(String host, int port, Controller controller) {
        this.host = host;
        this.port = port;
        this.controller = controller;
    }


    /**
     * Starts a connection-attempt to the server.
     * Once a connection is established an initial request is sent to the server. The connection is kept
     * until either this client or the server (robots) sends a 'SFIN'- (instruction Sequence FINished) or
     * 'DCNT'- (DisCoNnecT) signal.
     */
    @Override
    public void run() {
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);
            socket.setSoTimeout(TRANSMISSION_TIMEOUT);
            socket.setTcpNoDelay(true);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request = controller.getInitialRequest();
            String response;

            do {
                out.println(request);
                do {
                    response = in.readLine();
                    if (response == null) {
                        continue;
                    }
                    controller.handleResponse(response);
                    if (response.contains(INSTRUCTION_SEQUENCE_FINISHED)) {
                        break;
                    }
                } while (true);
                request = controller.getNextRequest();
            } while ( running && !request.equals(BOT_DISCONNECT));

            out.println(BOT_DISCONNECT);
            out.close();
            in.close();
            socket.close();
            running = true;
        } catch (SocketTimeoutException e) {

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
