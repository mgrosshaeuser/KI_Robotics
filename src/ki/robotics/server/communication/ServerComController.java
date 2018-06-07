package ki.robotics.server.communication;

public interface ServerComController {
    /**
     * Termination of current connection.
     */
    void disconnect();


    /**
     * Shutdown of the server.
     */
    void shutdown();


    void handleRequest(String transmission);
}
