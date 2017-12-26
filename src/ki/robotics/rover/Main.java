package ki.robotics.rover;

import lejos.robotics.navigation.Pose;


/**
 * Main class of the Servert. Communication-Port is specified and an instance of the BotServer is acquired..
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    private static final int PORT = 9999;
    private static BotServer server;


    /**
     * Program-Initialization and start of the server.
     * @param args
     */
    public static void main(String[] args) {
        boolean isSimulation = Boolean.parseBoolean(args[0].toLowerCase());
        server = new BotServer(PORT, isSimulation);
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
