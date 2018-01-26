package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;
import ki.robotics.client.MCL.MCL_Display;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.crisp.InstructionSetTranscoder;

import java.util.Random;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * Monte-Carlo-Localization including a GUI for presentation of results.
 *
 * @version 1.2, 01/02/18
 */
public class GUIComController implements ComController {
    private final MCL_Display window;
    private MCL_Provider mclProvider;
    private final InstructionSetTranscoder transcoder;
    private final SensorModel roverModel;
    private Configuration configuration;
    private Thread t;



    /**
     * Constructor.
     */
    public GUIComController() {
        this.window = new MCL_Display(this);
        this.transcoder = new InstructionSetTranscoder();
        this.roverModel = new SensorModel();
    }


    /**
     * Stars a new thread to handle the communication with the server (robot).
     *
     * @param configuration
     */
    @Override
    public void start(Configuration configuration) {
        this.mclProvider = window.getMclProvider();
        this.configuration = configuration;
        t = new Thread(new Communicator(Main.HOST, Main.PORT, this));
        t.setDaemon(true);
        t.start();
    }


    /**
     * Stops the communications-thread.
     */
    @Override
    public void stop() {
        if (t != null) {
            Communicator.running = false;
        }
    }


    /**
     * Returns the initial request, supposed to be sent to the robot right after the connection is established.
     *
     * @return  The initial request to the robot.
     */
    @Override
    public String getInitialRequest() {
        return SENSOR_RESET;
    }


    /**
     * Formulates the next request based on the responses that arrived so far.
     * Called from the server every time a previous instruction sequence was completed.
     *
     * @return  Ongoing instructions-requests.
     */
    @Override
    public String getNextRequest() {
        int bumper = 8;

        StringBuilder scans = new StringBuilder();
        for (String s : configuration.getSensingInstructions()) {
            scans.append(s).append(", ");
        }
        if (roverModel.getDistanceToLeft() == 0 && roverModel.getDistanceToRight() == 0 && roverModel.getDistanceToCenter() == 0) {
            return scans + MEASURE_COLOR;
        }

        if (configuration.isOneDimensional()) {
            //TODO: LineFolloweer + umdrehen wenn Linie zu ende
            return BOT_TRAVEL_FORWARD + configuration.getStepsize() + ", " + scans +  MEASURE_COLOR;

        } else{
            double center = roverModel.getDistanceToCenter(), left = roverModel.getDistanceToLeft(), right = roverModel.getDistanceToRight();
            if(center > bumper){
                return BOT_TRAVEL_FORWARD + " " + configuration.getStepsize() + ", " + scans + MEASURE_COLOR;
            }else if(left < right){
                return BOT_TURN_RIGHT + " 90, " + BOT_TRAVEL_FORWARD + " " + configuration.getStepsize() + ", " + scans + MEASURE_COLOR;
            }else{
                return BOT_TURN_LEFT + " 90, " + BOT_TRAVEL_FORWARD + configuration.getStepsize() + ", " + scans + MEASURE_COLOR;
            }
        }



    }



    /**
     * Handles the responses from the robot, which primarily means updating the sensor-model and reporting
     * to the MCL-Provider and repainting the display.
     *
     * @param response  A single response from the robot.
     */
    @Override
    public void handleResponse(String response) {
        System.out.println(response);
        if(response.equals(INSTRUCTION_SEQUENCE_FINISHED)){
            System.out.println();
        }
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
            case N_WAY_SCAN:
                measureDistance1D(statusCode);
                mclProvider.recalculateParticleWeight(roverModel);
                break;
            default:
                break;
            }
        window.repaint();
    }

    private void measureDistance1D(Instruction statusCode) {
        float angle = roverModel.getSensorHeadPosition();
        boolean measurementLeft = angle > 45;
        boolean measurementRight = angle < -45;

        if (measurementLeft) {
            roverModel.setDistanceToLeft((float)statusCode.getParameter());
        } else if (measurementRight) {
            roverModel.setDistanceToRight((float)statusCode.getParameter());
        } else {
            roverModel.setDistanceToCenter((float)statusCode.getParameter());
        }
    }
}
