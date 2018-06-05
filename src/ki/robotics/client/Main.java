package ki.robotics.client;


import ki.robotics.client.communication.ClientComController;


import java.io.IOException;
import java.net.InetAddress;

/**
 * Main class of the Control-Communicator. Host and port are specified.
 *
 */
public class Main {
    public static final String HOST;
    public static final int PORT = 9999;

    static {
        int twoSecondTimeout = 2000;
        String hostToTry = "10.0.1.3";
        String hostToSet;

        try {
            if (InetAddress.getByName(hostToTry).isReachable(twoSecondTimeout)) {
                hostToSet = hostToTry;
            } else {
                hostToSet = "localhost";
            }
        } catch (IOException e) {
            hostToSet = "localhost";
            e.printStackTrace();
        }
        HOST = hostToSet;
    }


    /**
     * Program-Initialization and start of a Communicator.
     *
     * @param args  String-argument to choose between graphical- and terminal-client
     */
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("terminal")) {
            ClientComController clientComController = ClientFactory.createNewTerminalClient();
            clientComController.start();
        } else {
            ClientComController clientComController = ClientFactory.createNewGraphicalClient();
        }
    }
}