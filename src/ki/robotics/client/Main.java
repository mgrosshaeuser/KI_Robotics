package ki.robotics.client;


/**
 * Main class of the Control-Communicator. Host and port are specified.
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    public static final String HOST = "192.168.43.171";
    public static final int PORT = 9999;



    /**
     * Program-Initialization and start of a Communicator.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length >=1 && args[0].equals("terminal")) {
            ComController comController = new TerminalComController();
            comController.start(null);
        } else {
            ComController comController = new GUIComController();
        }
    }
}
