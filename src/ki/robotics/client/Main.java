package ki.robotics.client;


/**
 * Main class of the Control-ControllerClient. Host and port are specified.
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    public static final String HOST = "dev";
    public static final int PORT = 9999;



    /**
     * Program-Initialization and start of a ControllerClient.
     *
     * @param args
     */
    public static void main(String[] args) {
        Controller controller = new Controller();
    }
}
