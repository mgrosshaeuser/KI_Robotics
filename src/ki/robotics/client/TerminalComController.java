package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;
import static ki.robotics.utility.crisp.CRISP.*;

import java.util.Scanner;

public class TerminalComController implements ComController{
    private Thread t;
    @Override
    public void start(Configuration configuration) {
        System.out.println("Connecting ...");
        t = new Thread(new ControllerClient(Main.HOST, Main.PORT, this));
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void stop() {
        if (t != null) {
            ControllerClient.running = false;
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
        return in.nextLine();
    }

    @Override
    public void handleResponse(String response) {
        System.out.println(response);
    }
}
