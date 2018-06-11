package ki.robotics.client;


import java.io.IOException;
import java.net.InetAddress;

/**
 * Main class of the Control-Communicator. Host and port are specified.
 *
 */
public class Main {
    private static final String HOST;
    private static final int PORT = 9999;

    static {
        int twoSecondTimeout = 2000;
        String hostToTry = "192.168.43.171";
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
            ClientFactory.createNewTerminalClient(HOST, PORT);
        } else {
            ClientFactory.createNewGraphicalClient(HOST, PORT);
        }
    }
}