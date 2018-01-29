package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;
import ki.robotics.client.MCL.MCL_Display;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.crisp.InstructionSetTranscoder;

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
     * @param configuration A run-configuration, chosen by the user.
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
            return scans + SENSOR_MEASURE_COLOR;
        }

        if (configuration.isOneDimensional()) {
            //TODO: LineFolloweer + umdrehen wenn Linie zu ende
            return BOT_TRAVEL_FORWARD + configuration.getStepsize() + ", " + scans + SENSOR_MEASURE_COLOR;

        } else{
            double center = roverModel.getDistanceToCenter(), left = roverModel.getDistanceToLeft(), right = roverModel.getDistanceToRight();
            if(center > bumper){
                return BOT_TRAVEL_FORWARD + " " + configuration.getStepsize() + ", " + scans + SENSOR_MEASURE_COLOR;
            }else if(left < right){
                return BOT_TURN_RIGHT + " 90, " + BOT_TRAVEL_FORWARD + " " + configuration.getStepsize() + ", " + scans + SENSOR_MEASURE_COLOR;
            }else{
                return BOT_TURN_LEFT + " 90, " + BOT_TRAVEL_FORWARD + configuration.getStepsize() + ", " + scans + SENSOR_MEASURE_COLOR;
            }
        }



    }



    /**
     * Routes the response from the robot to response-handling methods and repaints the display.
     *
     * @param botResponse  A single response from the robot.
     */
    @Override
    public void handleResponse(String botResponse) {
        System.out.println(botResponse);

        Instruction response = transcoder.decodeInstruction(botResponse);
        switch (response.getInstructionGroup()) {
            case BOT_INSTRUCTION:
                handleBotResponse(response);
                break;
            case SENSOR_INSTRUCTION:
                handleSensorResponse(response);
                break;
            case CAMERA_INSTRUCTION:
                handleCameraResponse(response);
                break;
            default:
                handleOtherResponse(response);
                break;
        }
        window.repaint();
    }


    /**
     * Handles responses regarding movement of the robot.
     *
     * @param response   The instruction-response.
     */
    private void handleBotResponse(Instruction response) {
        double parameter = ((Instruction.SingleFloatInstruction)response).getParameter();
        switch (response.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                mclProvider.translateParticle((float)parameter);
                break;
            case BOT_TRAVEL_BACKWARD:
                mclProvider.translateParticle((float)(-parameter));
                break;
            case BOT_TURN_LEFT:
                mclProvider.turnFull((int) Math.abs(parameter));
                break;
            case BOT_TURN_RIGHT:
                mclProvider.turnFull((int) Math.abs(parameter) * -1);
                break;
        }
    }


    /**
     * Handles responses regarding the sensors and turning of the sensor-head.
     *
     * @param response  The instruction-response.
     */
    private void handleSensorResponse(Instruction response) {
        switch (response.getMnemonic()) {
            case SENSOR_TURN_LEFT:
                int leftAngle = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setSensorHeadPosition(leftAngle);
                break;
            case SENSOR_TURN_RIGHT:
                int rightAngle = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setSensorHeadPosition(rightAngle);
                break;
            case SENSOR_MEASURE_COLOR:
                int color = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setColor(color);
                break;
            case THREE_WAY_SCAN_LEFT:
                double distanceLeft = ((Instruction.SingleFloatInstruction)response).getParameter();
                roverModel.setDistanceToLeft((float) distanceLeft);
                break;
            case THREE_WAY_SCAN_CENTER:
                double distanceCenter = ((Instruction.SingleFloatInstruction)response).getParameter();
                roverModel.setDistanceToLeft((float) distanceCenter);
                break;
            case THREE_WAY_SCAN_RIGHT:
                double distanceRight = ((Instruction.SingleFloatInstruction)response).getParameter();
                roverModel.setDistanceToLeft((float) distanceRight);
                break;
            case SENSOR_THREE_WAY_SCAN:
                mclProvider.recalculateParticleWeight(roverModel);
                break;
            case SENSOR_RESET:
                roverModel.setSensorHeadPosition(0);
                break;
            case SENSOR_SINGLE_DISTANCE_SCAN:
                measureDistance1D(response);
                mclProvider.recalculateParticleWeight(roverModel);
                break;
        }
    }


    /**
     * Handles responses regarding the camera.
     *
     * @param response  The instruction-response.
     */
    private  void handleCameraResponse(Instruction response) {
        //TODO Implementation
    }


    /**
     * Handles all other responses.
     *
     * @param response  The instruction-response.
     */
    private void handleOtherResponse(Instruction response) {
        switch (response.getMnemonic()) {
            case END_OF_INSTRUCTION_SEQUENCE:
                System.out.println();
                break;
        }
    }


    /**
     * Updates the sensor-model for left, front or right distance from a single distance measurement, depending
     * on the current orientation of the sensor-head.
     *
     * @param instruction   The instruction-response.
     */
    private void measureDistance1D(Instruction instruction) {
        float angle = roverModel.getSensorHeadPosition();
        boolean measurementLeft = angle > 45;
        boolean measurementRight = angle < -45;

        double param = ((Instruction.SingleFloatInstruction)instruction).getParameter();

        if (measurementLeft) {
            roverModel.setDistanceToLeft((float) param);
        } else if (measurementRight) {
            roverModel.setDistanceToRight((float)param);
        } else {
            roverModel.setDistanceToCenter((float)param);
        }
    }
}
