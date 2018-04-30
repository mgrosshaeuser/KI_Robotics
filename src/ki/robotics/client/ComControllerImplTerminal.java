package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;

import static ki.robotics.utility.crisp.CRISP.*;

import java.util.Scanner;

public class ComControllerImplTerminal implements ComController{
    private Thread t;
    @Override
    public void start(Configuration configuration) {
        System.out.println("I.R.I.S - Interactive Robot Instruction Shell");
        System.out.print("---------------------------------------------\n\n");
        t = new Thread(new Communicator(Main.HOST, Main.PORT, this));
        t.start();
    }

    @Override
    public void stop() {
        if (t != null) {
            Communicator.running = false;
        }
    }

    @Override
    public String getInitialRequest() {
        return SENSOR_RESET;
    }

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

    @Override
    public void handleResponse(String response) {
        System.out.println(response);
    }
}
