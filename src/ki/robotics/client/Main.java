package ki.robotics.client;


import java.io.IOException;
import java.net.InetAddress;

/**
 * Main class of the Control-Communicator. Host and port are specified.
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    public static final String HOST;
    public static final int PORT = 9999;

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
     * @param args
     */
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("terminal")) {
            ComController comController = new TerminalComController();
            comController.start(null);
        } else {
            ComController comController = new GUIComController();
        }
    }
}