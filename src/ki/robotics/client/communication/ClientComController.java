package ki.robotics.client.communication;

public interface ClientComController {
    /**
     * Initiates communication with the server. Preferably using a new thread.
     */
    void start();

    /**
     * Stops the communication with the server.
     */
    void stop();


    /**
     * Permits a communication-thread to ask whether communication should stop or continue.
     *
     * @return A boolean value indicating whether to stop (true) or continue (false) communication
     */
    boolean isStopped();


    /**
     * Returns the initial request that is supposed to be sent right after the connection is established.
     *
     * @return  The initial request to be sent
     */
    String getInitialRequest();


    /**
     * Returns the next request. Supposed to realize an unlimited send-receive-cycle with handleResponse(String).
     *
     * @return  The next request.
     */
    String getNextRequest();


    /**
     * Handles a response. Supposed to realize an unlimited send-receive-cycle with getNextRequest():String.
     *
     */
    void handleResponse(String response);
}
