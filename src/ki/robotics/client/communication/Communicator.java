package ki.robotics.client.communication;

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
    private final ComController comController;

    static volatile boolean running = true;


    /**
     * Constructor.
     *
     * @param host  The host to which to connect
     * @param port  The port to address
     */
    public Communicator(String host, int port, ComController ComController) {
        this.host = host;
        this.port = port;
        this.comController = ComController;
    }


    /**
     * Initiates the connection with the server, delegates the ongoing communication and the teardown
     * of the connection.
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


    /**
     * Creates the communication-Socket
     *
     * @param host  The host to which to connect
     * @param port  The port to address
     * @return      A socket for port at host
     * @throws IOException
     */
    private Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setKeepAlive(true);
        socket.setSoTimeout(TRANSMISSION_TIMEOUT);
        socket.setTcpNoDelay(true);
        return socket;
    }


    /**
     * Handles the ongoing communication with the server.
     * Once a connection is established an initial request is sent to the server. The connection is kept
     * until either this client or the server (robots) sends a 'EOSQ'- (End Of SeQuence) or
     * 'DCNT'- (DisCoNnecT) signal.
     *
     * @param in    BufferedReader to read responses from
     * @param out   PrintWriter to write requests (instructions) to
     * @throws IOException
     */
    private void handleOngoingCommunication(BufferedReader in, PrintWriter out) throws IOException {
        String request = comController.getInitialRequest();
        String response;

        do {
            out.println(request);
            do {
                response = in.readLine();
                if (response == null) {
                    continue;
                }
                comController.handleResponse(response);
                if (response.contains(END_OF_INSTRUCTION_SEQUENCE)) {
                    break;
                }
            } while (true);
            while (!comController.isStopped()   &&   (request = comController.getNextRequest()) == null) {
                Thread.yield();
            }
            if (comController.isStopped()) {
                break;
            }
        } while ( running && !request.equals(DISCONNECT));
    }


    /**
     * Closes the Socket, the BufferedReader and the PrintWriter after usage.
     *
     * @param socket    The communication-socket
     * @param in        The BufferedReader used for communication
     * @param out       The PrintWriter used for communication
     * @throws IOException
     */
    private void tearDownConnection(Socket socket, BufferedReader in, PrintWriter out) throws IOException{
        out.println(DISCONNECT);
        out.close();
        in.close();
        socket.close();
        running = true;
    }

}
