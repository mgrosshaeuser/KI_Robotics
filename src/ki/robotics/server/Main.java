package ki.robotics.server;


import ki.robotics.server.communication.ServerCommunicator;

/**
 * Main class of the Server. Communication-Port is specified and an instance of the BotServer is acquired..
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    private static final int PORT = 9999;
    private static ServerCommunicator server;


    /**
     * Program-Initialization and start of the server.
     * @param args  Command line argument; 'true' for simulation-mode.
     */
    public static void main(String[] args) {
        boolean isSimulation = false;
        if (args.length >=1) {
            isSimulation = Boolean.parseBoolean(args[0].toLowerCase());
        }
        server = ServerFactory.createNewServerCommunicator(PORT, isSimulation);
        server.powerUp();
    }



    /**
     * Allows the robot to disconnect from the client.
     */
    public static void disconnect() {
        server.disconnect();
    }



    /**
     * Allows the robot to shutdown the server.
     */
    public static void shutdown() {
        server.shutdown();
    }

}
