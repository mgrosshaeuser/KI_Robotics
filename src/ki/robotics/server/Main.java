package ki.robotics.server;


import ki.robotics.server.communication.ServerCommunicator;

/**
 * Main class of the Server. Communication-Port is specified and an instance of the BotServer is acquired..
 *
 */
public class Main {
    private static final int PORT = 9999;


    /**
     * Program-Initialization and start of the server.
     * @param args  Command line argument; 'true' for simulation-mode.
     */
    public static void main(String[] args) {
        boolean isSimulation = false;
        if (args.length >=1) {
            isSimulation = Boolean.parseBoolean(args[0].toLowerCase());
        }

        ServerCommunicator server = ServerFactory.createNewServerCommunicator(PORT, isSimulation);
        server.powerUp();
    }
}
