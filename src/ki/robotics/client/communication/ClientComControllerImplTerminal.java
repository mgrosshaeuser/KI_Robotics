package ki.robotics.client.communication;

import static ki.robotics.utility.crisp.CRISP.*;

import java.util.Scanner;

/**
 * A terminal for communication with the server (robot).
 */
public final class ClientComControllerImplTerminal extends ClientComControllerImpl {
    private Thread t;


    /**
     * Constructor.
     *
     * @param host  Server-host
     * @param port  Server-port
     */
    public ClientComControllerImplTerminal(String host, int port) {
        super(host, port);
        start();
    }


    /**
     * Starts a new thread to handle the communication with the server (robot).
     */
    @Override
    public void start() {
        System.out.println("I.R.I.S - Interactive Robot Instruction Shell");
        System.out.print("---------------------------------------------\n\n");
        t = new Thread(new ClientCommunicator(this.host, this.port, this));
        t.start();
    }


    /**
     * Stops the communications-thread.
     */
    @Override
    public void stop() {
        if (t != null) {
            ClientCommunicator.running = false;
        }
    }


    /**
     * Allows the communication-thread to ask about the communication-status (stopped or ongoing).
     *
     * @return  boolean value to indicate stopped (true) or ongoing (false)
     */
    @Override
    public boolean isStopped() {
        return false;
    }


    /**
     * Returns the initial request, supposed to be sent to the robot right after the connection is established.
     *
     * @return  The initial request to the robot
     */
    @Override
    public String getInitialRequest() {
        return SENSOR_RESET;
    }


    /**
     * Prints a commandline-prompt and waits for user-input
     * Called from the server every time a previous instruction sequence was completed.
     *
     * @return  User-input (instruction)
     */
    @Override
    public String getNextRequest() {
        Scanner in = new Scanner(System.in);
        System.out.print("\n\n>>> ");
        String cmd = in.nextLine();
        if (cmd.equals(DISCONNECT)) {
            stop();
        }
        return cmd;
    }


    /**
     * Prints the server-response to the terminal.
     *
     * @param response  The server-response
     */
    @Override
    public void handleResponse(String response) {
        System.out.println(response);
    }
}
