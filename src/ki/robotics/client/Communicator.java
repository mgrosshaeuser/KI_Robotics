package ki.robotics.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static ki.robotics.utility.crisp.CRISP.DISCONNECT;
import static ki.robotics.utility.crisp.CRISP.END_OF_INSTRUCTION_SEQUENCE;


/**
 * Communication-Instance for the client-side.
 *
 */
final class Communicator implements Runnable{

    private static final int TRANSMISSION_TIMEOUT = 0;

    private final String host;
    private final int port;
    private final ComController ComController;

    public static volatile boolean running = true;


    /**
     * Constructor.
     *
     * @param host  The host to which to connect.
     * @param port  The port to address.
     */
    public Communicator(String host, int port, ComController ComController) {
        this.host = host;
        this.port = port;
        this.ComController = ComController;
    }


    /**
     * Starts a connection-attempt to the server.
     * Once a connection is established an initial request is sent to the server. The connection is kept
     * until either this client or the server (robots) sends a 'EOSQ'- (End Of SeQuence) or
     * 'DCNT'- (DisCoNnecT) signal.
     */
    @Override
    public void run() {
        try {
            Socket socket = createSocket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            handleOngoingCommunication(in, out);
            tearDownConnection(socket, in, out);
        } catch (SocketTimeoutException ignored) {

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }



    private Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setKeepAlive(true);
        socket.setSoTimeout(TRANSMISSION_TIMEOUT);
        socket.setTcpNoDelay(true);
        return socket;
    }



    private void handleOngoingCommunication(BufferedReader in, PrintWriter out) throws IOException {
        String request = ComController.getInitialRequest();
        String response;

        do {
            out.println(request);
            do {
                response = in.readLine();
                if (response == null) {
                    continue;
                }
                ComController.handleResponse(response);
                if (response.contains(END_OF_INSTRUCTION_SEQUENCE)) {
                    break;
                }
            } while (true);
            request = ComController.getNextRequest();
        } while ( running && !request.equals(DISCONNECT));
    }



    private void tearDownConnection(Socket socket, BufferedReader in, PrintWriter out) throws IOException{
        out.println(DISCONNECT);
        out.close();
        in.close();
        socket.close();
        running = true;
    }

}
