package ki.robotics.server.communication;

public interface ServerCommunicator {
    /**
     * Registers a communication-controller as an intermediate between communicator and robot.
     *
     * @param comController A communication-controller
     */
    void registerComController(ServerComControllerImpl comController);


    /**
     * Starts the server.
     */
    void powerUp();


    /**
     * Termination of current connection.
     */
    void disconnect();


    /**
     * Shutdown of the server.
     */
    void shutdown();
}
