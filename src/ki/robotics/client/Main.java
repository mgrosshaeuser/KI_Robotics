package ki.robotics.client;


import java.io.IOException;
import java.net.InetAddress;

/**
 * Main class of the Control-Communicator. Host and port are specified.
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    public static String HOST = "localhost";
    public static final int PORT = 9999;

    /**
     * Program-Initialization and start of a Communicator.
     *
     * @param args
     */
    public static void main(String[] args) {
        setHOST();
        if (args.length >=1 && args[0].equals("terminal")) {
            ComController comController = new TerminalComController();
            comController.start(null);
        } else {
            ComController comController = new GUIComController();
        }
    }

    /**
     * sets HOST to EV3 adress if EV3 is reachable
     * else uses already set HOST
     */
    private static void setHOST() {
        int oneSecondTimeout = 1000;
        String hostToTry = "192.168.43.171";
        try {
            if (InetAddress.getByName(hostToTry).isReachable(oneSecondTimeout)) {
                HOST = hostToTry;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}