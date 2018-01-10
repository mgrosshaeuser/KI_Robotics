package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;
import ki.robotics.client.MCL.MCL_Display;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.crisp.InstructionSetTranscoder;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * Monte-Carlo-Localization including a GUI for presentation of results.
 *
 * @version 1.2, 01/02/18
 */
public class Controller {
    private MCL_Display window;
    private MCL_Provider mclProvider;
    private InstructionSetTranscoder transcoder;
    private SensorModel roverModel;
    private Configuration configuration;

    private Thread t;



    /**
     * Constructor.
     */
    public Controller() {
        this.window = new MCL_Display(this);
        this.transcoder = new InstructionSetTranscoder();
        this.roverModel = new SensorModel();
    }


    /**
     * Stars a new thread to handle the communication with the server (robot).
     *
     * @param configuration
     */
    public void start(Configuration configuration) {
        this.mclProvider = window.getMclProvider();
        this.configuration = configuration;
        t = new Thread(new ControllerClient(Main.HOST, Main.PORT, this));
        t.setDaemon(true);
        t.start();
    }


    /**
     * Stops the communications-thread.
     */
    public void stop() {
        if (t != null) {
            ControllerClient.running = false;
        }
    }


    /**
     * Returns the initial request, supposed to be sent to the robot right after the connection is established.
     *
     * @return  The initial request to the robot.
     */
    public String getInitialRequest() {
        return SENSOR_RESET;
    }


    /**
     * Formulates the next request based on the responses that arrived so far.
     * Called from the server every time a previous instruction sequence was completed.
     *
     * @return  Ongoing instructions-requests.
     */
    public String getNextRequest() {
        int minWallDistance = 15;

        String direction = "";

        if (configuration.isTwoDimensional()) {
            Random r = new Random();
            int d = (r.nextInt() % 3);
            if (d < -1) {
                direction = BOT_TURN_RIGHT + "90, ";
            } else if (d > 1) {
                direction = BOT_TURN_LEFT + "90, ";
            } else {
                if (! (roverModel.getDistanceToCenter() > minWallDistance) ){
                    if (roverModel.getDistanceToRight() > minWallDistance) {
                        direction = BOT_TURN_RIGHT + " 90, ";
                    } else {
                        direction = BOT_TURN_LEFT + " 90, ";
                    }
                }
            }
        }

        StringBuilder scans = new StringBuilder();
        for (String s : configuration.getSensingInstructions()) {
            scans.append(s).append(", ");
        }

        return direction + BOT_TRAVEL_FORWARD + configuration.getStepsize() +", " + scans + MEASURE_COLOR;
    }



    /**
     * Handles the responses from the robot, which primarily means updating the sensor-model and reporting
     * to the MCL-Provider and repainting the display.
     *
     * @param response  A single response from the robot.
     */
    public void handleResponse(String response) {
        Instruction statusCode = transcoder.decodeInstruction(response);
        switch (statusCode.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                mclProvider.translateParticle((float)statusCode.getParameter());
                break;
            case BOT_TURN_LEFT:
                mclProvider.turnFull((int) Math.abs(statusCode.getParameter()));
                break;
            case BOT_TURN_RIGHT:
                mclProvider.turnFull((int) Math.abs(statusCode.getParameter()) * -1);
                break;
            case SENSOR_TURN_LEFT:
                roverModel.setSensorHeadPosition((float)statusCode.getParameter());
                break;
            case SENSOR_TURN_RIGHT:
                roverModel.setSensorHeadPosition((float)statusCode.getParameter() * -1);
                break;
            case MEASURE_COLOR:
                roverModel.setColor(Integer.parseInt(response.substring(4).trim()));
                break;
            case THREE_WAY_SCAN_LEFT:
                roverModel.setDistanceToLeft((float)statusCode.getParameter());
                break;
            case THREE_WAY_SCAN_CENTER:
                roverModel.setDistanceToCenter((float)statusCode.getParameter());
                break;
            case THREE_WAY_SCAN_RIGHT:
                roverModel.setDistanceToRight((float)statusCode.getParameter());
                break;
            case THREE_WAY_SCAN:
                mclProvider.recalculateParticleWeight(roverModel);
                break;
            case SENSOR_RESET:
                roverModel.setSensorHeadPosition(0);
            case MEASURE_DISTANCE:
                float angle = roverModel.getSensorHeadPosition();
                if (angle > 45) {
                    roverModel.setDistanceToLeft((float)statusCode.getParameter());
                } else if (angle < 45) {
                    roverModel.setDistanceToRight((float)statusCode.getParameter());
                } else {
                    roverModel.setDistanceToCenter((float)statusCode.getParameter());
                }
                mclProvider.recalculateParticleWeight(roverModel);
                break;
            default:
                break;
            }
        window.repaint();
    }

}
