package ki.robotics.client.communication;

/**
 * Abstract base for a communication-controller communicating with the server 'host' over port 'port'.
 */
abstract class ClientComControllerImpl implements ClientComController {
    String host;
    int port;

    /**
     * Constructor.
     *
     * @param host  Server
     * @param port  Server-port
     */
    ClientComControllerImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
