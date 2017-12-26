package ki.robotics.mission_control;


/**
 * Main class of the Control-Client. Host and port are specified.
 *
 * @version 1.0, 12/26/17
 */
public class Main {
    public static final String HOST = "dev";
    public static final int PORT = 9999;



    /**
     * Program-Initialization and start of a ControlClient.
     *
     * @param args
     */
    public static void main(String[] args) {
        ControlClient client = new ControlClient(HOST, PORT);
        client.powerUp();
    }
}
